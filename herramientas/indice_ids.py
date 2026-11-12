#!/usr/bin/env python3
"""Índice de IDs de RutaUN: derivado, regenerable y nunca fuente de verdad.

Recorre `docs/requisitos/` (dueños de cada ID), el resto de la documentación,
el código y los nombres de las pruebas, y responde por ID: dónde vive su texto
normativo, qué otros IDs y términos del glosario cita, y quién lo menciona.

    herramientas/indice_ids.py                       resumen y problemas
    herramientas/indice_ids.py UC06-HU01             ficha del ID
    herramientas/indice_ids.py "UC06 ext. 4a"        ficha de una extensión
    herramientas/indice_ids.py "«Malla utilizable»"  ficha de un término
    herramientas/indice_ids.py UC06-HU01 --contexto  texto verbatim del ID, de lo
                                                     que cita y de sus términos
    herramientas/indice_ids.py --decisiones          decisiones de diseño y su estado
    herramientas/indice_ids.py 0001                  ficha de una decisión
    herramientas/indice_ids.py --json [ruta]         índice completo
                                                     (target/indice-ids.json)
    herramientas/indice_ids.py --verificar           falla si hay referencias sin
                                                     dueño o dueños repetidos

No escribe nada en `docs/`: la salida va a la consola o a `target/`, que git
ignora. Solo usa la biblioteca estándar de Python 3.
"""

from __future__ import annotations

import json
import re
import subprocess
import sys
import unicodedata
from collections import defaultdict
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
REQ = RAIZ / "docs" / "requisitos"
DECISIONES = RAIZ / "docs" / "diseno" / "decisiones"
SALIDA_JSON = RAIZ / "target" / "indice-ids.json"
EXTENSIONES = (".java", ".md", ".html", ".sql")

SECCIONES = {
    "Garantía mínima": "GM",
    "Garantía de éxito": "GE",
    "Escenario principal": "paso",
    "Extensiones": "ext.",
}
CLAVES = {"paso": "paso", "pasos": "paso", "ext.": "ext.", "gm": "GM", "ge": "GE"}

RE_UC = re.compile(r"\bUC(0[1-7])\b")
RE_HU = re.compile(r"\bUC(0[1-7])-HU(\d\d)(-T\d+)?\b")
RE_CLAVE = re.compile(
    r"\b(pasos?|ext\.|extensi[oó]n(?:es)?|GM|GE)\s+"
    r"(\d+[a-z]?(?:(?:, | y | o | a |\s*(?:–|-)\s*)\d+[a-z]?)*)(?![\w])"
)
RE_SUPUESTO = re.compile(r"(?<![\w-])S(1[0-8]|[1-9])\b(?!-)")
RE_TAREA = re.compile(r"\b(TT|SOP|PRU|DOC|ENT)-(\d\d)(-T\d+)?\b")
RE_ISSUE = re.compile(r"(?<![\w&#])#(\d{1,3})\b(?![\w-])")
RE_CITA_TERMINO = re.compile(r"«([^»]+)»")
RE_PRUEBA = re.compile(r"\bvoid\s+([a-z][A-Za-z0-9_]*)\s*\(")


def normalizar(texto: str) -> str:
    sin_tildes = unicodedata.normalize("NFD", texto)
    return "".join(c for c in sin_tildes if unicodedata.category(c) != "Mn").lower()


def relativo(ruta: Path) -> str:
    return ruta.relative_to(RAIZ).as_posix()


def cuerpo(ruta: Path) -> list[str]:
    """Líneas del archivo, con el frontmatter YAML en blanco (conserva los números)."""
    lineas = ruta.read_text(encoding="utf-8").splitlines()
    if lineas and lineas[0] == "---":
        for i in range(1, len(lineas)):
            if lineas[i] == "---":
                return [""] * (i + 1) + lineas[i + 1 :]
    return lineas


def frontmatter(ruta: Path) -> dict:
    """Claves del frontmatter YAML: `clave: valor`, `clave: [a, b]` y listas con guion."""
    lineas = ruta.read_text(encoding="utf-8").splitlines()
    datos: dict = {}
    if not lineas or lineas[0] != "---":
        return datos
    clave = None
    for linea in lineas[1:]:
        if linea == "---":
            break
        linea = linea.split(" #")[0].rstrip()
        if m := re.match(r"\s+- (.*)", linea):
            if clave:
                datos.setdefault(clave, [])
                if isinstance(datos[clave], list):
                    datos[clave].append(m[1].strip().strip('"'))
        elif m := re.match(r"([A-Za-z_]+):\s*(.*)", linea):
            clave, valor = m[1], m[2].strip()
            if valor.startswith("["):
                datos[clave] = [v.strip().strip('"') for v in valor.strip("[]").split(",") if v.strip()]
            elif valor:
                datos[clave] = valor.strip('"')
            else:
                datos[clave] = []
    return datos


# ---------------------------------------------------------------- dueños


class Indice:
    def __init__(self) -> None:
        self.ids: dict[str, dict] = {}
        self.terminos: dict[str, dict] = {}
        self.decisiones: dict[str, dict] = {}
        self.problemas: list[str] = []
        self._alias: list[tuple[re.Pattern, str]] = []
        self._alias_exacto: dict[str, str] = {}

    def registrar(self, ident, tipo, ruta, desde, hasta, titulo="", **extra):
        if ident in self.ids and self.ids[ident]["dueno"]:
            previo = self.ids[ident]["dueno"]
            self.problemas.append(
                f"dueño repetido: {ident} en {previo['archivo']}:{previo['linea']}"
                f" y en {relativo(ruta)}:{desde}"
            )
            return
        self.ids[ident] = {
            "tipo": tipo,
            "titulo": titulo,
            "dueno": {"archivo": relativo(ruta), "linea": desde, "hasta": hasta},
            "cita": [],
            "terminos": [],
            "citado_en": {"requisitos": [], "docs": [], "codigo": [], "pruebas": []},
            **extra,
        }

    def nodo(self, ident: str) -> dict:
        """Nodo de un ID sin dueño en el SRS (tareas, issues, sub-issues)."""
        if ident not in self.ids:
            self.ids[ident] = {
                "tipo": tipo_de(ident),
                "titulo": "",
                "dueno": None,
                "cita": [],
                "terminos": [],
                "citado_en": {"requisitos": [], "docs": [], "codigo": [], "pruebas": []},
            }
        return self.ids[ident]


def tipo_de(ident: str) -> str:
    if ident.startswith("#"):
        return "issue"
    if RE_TAREA.fullmatch(ident):
        return "tarea"
    if re.fullmatch(r"UC\d\d-HU\d\d-T\d+", ident):
        return "sub-issue"
    if re.fullmatch(r"UC\d\d-HU\d\d", ident):
        return "historia"
    if re.fullmatch(r"S\d+", ident):
        return "supuesto"
    if re.fullmatch(r"UC\d\d", ident):
        return "caso"
    return "elemento"


def leer_casos(indice: Indice) -> None:
    for ruta in sorted((REQ / "casos").glob("UC0[1-7]-*.md")):
        uc = ruta.name[:4]
        lineas = cuerpo(ruta)
        total = len(lineas)
        seccion = None
        historia = None  # (id, issue, titulo, linea)

        def cerrar_historia(hasta: int) -> None:
            nonlocal historia
            if historia:
                ident, issue, titulo, desde = historia
                indice.registrar(
                    ident, "historia", ruta, desde, hasta, titulo, issue=f"#{issue}"
                )
                historia = None

        for n, linea in enumerate(lineas, 1):
            if linea.startswith("# "):
                indice.registrar(uc, "caso", ruta, n, total, linea[2:].strip())
            elif linea.startswith("### "):
                cerrar_historia(n - 1)
                seccion = None
                m = re.match(r"### (UC\d\d-HU\d\d) \(#(\d+)\) · (.*)", linea)
                if m:
                    historia = (m[1], m[2], m[3].strip(), n)
            elif linea.startswith("## ") or linea.strip() == "---":
                cerrar_historia(n - 1)
                seccion = next(
                    (v for k, v in SECCIONES.items() if linea[3:].startswith(k)), None
                )
            elif seccion in ("GM", "GE", "paso"):
                m = re.match(r"(\d+)\. ", linea)
                if m:
                    tipo = "paso" if seccion == "paso" else "garantia"
                    indice.registrar(f"{uc} {seccion} {m[1]}", tipo, ruta, n, n)
            elif seccion == "ext.":
                m = re.match(r"- \*\*(\d+[a-z])\*\* ·", linea)
                if m:
                    indice.registrar(f"{uc} ext. {m[1]}", "extension", ruta, n, n)
        cerrar_historia(total)


def leer_supuestos(indice: Indice) -> None:
    ruta = REQ / "04-supuestos.md"
    for n, linea in enumerate(cuerpo(ruta), 1):
        m = re.match(r"\| \*\*(S\d+)\*\* \|", linea)
        if m:
            indice.registrar(m[1], "supuesto", ruta, n, n)


def leer_glosario(indice: Indice) -> None:
    for ruta in sorted((REQ / "glosario").glob("0*.md")):
        lineas = cuerpo(ruta)
        inicios = [n for n, l in enumerate(lineas, 1) if l.startswith("## ")]
        for k, desde in enumerate(inicios):
            hasta = (inicios[k + 1] - 1) if k + 1 < len(inicios) else len(lineas)
            while hasta > desde and not lineas[hasta - 1].strip():
                hasta -= 1
            nombre = lineas[desde - 1][3:].strip()
            if nombre in indice.terminos:
                indice.problemas.append(f"término repetido: «{nombre}» en {relativo(ruta)}:{desde}")
                continue
            indice.terminos[nombre] = {
                "archivo": relativo(ruta),
                "linea": desde,
                "hasta": hasta,
                "cita_terminos": [],
                "usado_por": [],
            }
    alias: dict[str, str] = {}
    for nombre in indice.terminos:
        base = re.sub(r"\s*\(.*?\)", "", nombre)
        partes = {nombre, base}
        partes.update(p.strip() for p in re.split(r", | y ", base))
        m = re.search(r"\(o (.+?)\)", nombre)
        if m:
            partes.add(m[1])
        for parte in partes:
            alias.setdefault(normalizar(parte), nombre)
    indice._alias_exacto = alias
    # Los alias más largos primero: «Malla estándar» gana a «Malla».
    for clave in sorted(alias, key=len, reverse=True):
        palabras = [re.escape(p) + r"(?:s|es)?" for p in clave.split()]
        patron = re.compile(r"(?<!\w)" + r"\s+".join(palabras) + r"(?!\w)")
        indice._alias.append((patron, alias[clave]))


def leer_decisiones(indice: Indice) -> None:
    """Decisiones de diseño: `docs/diseno/decisiones/NNNN-….md`.

    El estado, la fecha, los IDs y lo que reemplaza se leen del frontmatter
    (`estado`, `fecha`, `srs_ids`, `reemplaza`). Si faltan, el estado y la fecha
    salen de la línea «Fecha: … · Estado: **…**» y los IDs, de los que el texto cita.
    """
    if not DECISIONES.is_dir():
        return
    for ruta in sorted(DECISIONES.glob("[0-9][0-9][0-9][0-9]-*.md")):
        numero = ruta.name[:4]
        datos = frontmatter(ruta)
        lineas = cuerpo(ruta)
        texto = "\n".join(lineas)
        titulo = datos.get("title") or next((l[2:].strip() for l in lineas if l.startswith("# ")), "")
        en_linea = re.search(r"Fecha:\s*(\d{4}-\d\d-\d\d)\s*·\s*Estado:\s*\*{0,2}([^*\n]+)", texto)
        declarados = datos.get("srs_ids")
        presente = isinstance(declarados, list)
        citados = list(dict.fromkeys(i for linea in lineas for i in menciones(linea)))
        reemplaza = datos.get("reemplaza") or []
        if isinstance(reemplaza, str):
            reemplaza = [reemplaza]
        indice.decisiones[numero] = {
            "archivo": relativo(ruta),
            "titulo": titulo,
            "estado": (datos.get("estado") or (en_linea[2].strip() if en_linea else "")).lower(),
            "fecha": datos.get("fecha") or (en_linea[1] if en_linea else ""),
            "ids": declarados if presente else citados,
            "ids_declarados": presente,
            "reemplaza": [str(r)[:4] for r in reemplaza],
            "reemplazada_por": [],
        }
    for numero, decision in indice.decisiones.items():
        if not decision["estado"]:
            indice.problemas.append(f"decisión sin estado: {decision['archivo']}")
        for otra in decision["reemplaza"]:
            if otra not in indice.decisiones:
                indice.problemas.append(f"decisión {numero} reemplaza a {otra}, que no existe")
                continue
            indice.decisiones[otra]["reemplazada_por"].append(numero)
            if "reemplazada" not in indice.decisiones[otra]["estado"]:
                indice.problemas.append(
                    f"decisión {otra}: la reemplaza {numero}, pero su estado no dice «reemplazada»")


# ------------------------------------------------------------- menciones


def expandir(clave: str, lista: str, uc: str) -> list[str]:
    """«ext. 2a, 2b y 2c» → [UCnn ext. 2a, UCnn ext. 2b, UCnn ext. 2c]."""
    tipo = CLAVES.get(clave.lower(), "ext.")
    fichas = re.findall(r"\d+[a-z]?", lista)
    rango = re.fullmatch(r"(\d+)([a-z]) a (\d+)([a-z])", lista)
    if rango and rango[1] == rango[3]:
        fichas = [f"{rango[1]}{chr(c)}" for c in range(ord(rango[2]), ord(rango[4]) + 1)]
    else:
        guion = re.fullmatch(r"(\d+)([a-z])?\s*(?:–|-|a)\s*(\d+)([a-z])?", lista)
        if guion:
            ini, li, fin, lf = guion.groups()
            if li and lf and ini == fin:
                fichas = [f"{ini}{chr(c)}" for c in range(ord(li), ord(lf) + 1)]
            elif not li and not lf and int(fin) >= int(ini):
                fichas = [str(n) for n in range(int(ini), int(fin) + 1)]
    return [f"{uc} {tipo} {f}" for f in fichas]


def menciones(linea: str, uc_contexto: str | None = None) -> list[str]:
    """IDs que nombra una línea. `uc_contexto` resuelve «paso 4» dentro de un caso."""
    hallados: list[str] = []
    for m in RE_HU.finditer(linea):
        hallados.append(m[0])
    for m in RE_UC.finditer(linea):
        hallados.append(m[0])
    previo_fin, previo_uc = -1, None
    for m in RE_CLAVE.finditer(linea):
        antes = linea[: m.start()]
        pegado = re.search(r"UC(0[1-7])[,:]?\s*\(?$", antes)
        if pegado:
            uc = f"UC{pegado[1]}"
        elif previo_uc and re.fullmatch(r"(,|;| y| o|,? y también)\s+", linea[previo_fin : m.start()]):
            uc = previo_uc
        elif uc_contexto:
            uc = uc_contexto
        else:
            cercanos = RE_UC.findall(antes)
            uc = f"UC{cercanos[-1]}" if cercanos else None
        if uc:
            hallados.extend(expandir(m[1], m[2], uc))
        previo_fin, previo_uc = m.end(), uc
    hallados.extend(f"S{m[1]}" for m in RE_SUPUESTO.finditer(linea))
    hallados.extend(m[0] for m in RE_TAREA.finditer(linea))
    hallados.extend(f"#{m[1]}" for m in RE_ISSUE.finditer(linea))
    return list(dict.fromkeys(hallados))


def ids_de_prueba(nombre: str) -> list[str]:
    """`uc06_ext4a_…` → [UC06, UC06 ext. 4a]; `tt02_uc06_…` → [TT-02, UC06]."""
    hallados: list[str] = []
    uc = hu = None
    for ficha in nombre.split("_")[:-1]:
        if m := re.fullmatch(r"uc(0[1-7])", ficha):
            uc, hu = f"UC{m[1]}", None
            hallados.append(uc)
        elif (m := re.fullmatch(r"hu(\d\d)", ficha)) and uc:
            hu = f"{uc}-HU{m[1]}"
            hallados.append(hu)
        elif (m := re.fullmatch(r"t(\d+)", ficha)) and hallados:
            hallados.append(f"{hallados[-1]}-T{m[1]}")
        elif (m := re.fullmatch(r"(ext|paso|gm|ge)(\d+[a-z]?)", ficha)) and uc:
            clave = {"ext": "ext.", "paso": "paso", "gm": "GM", "ge": "GE"}[m[1]]
            hallados.append(f"{uc} {clave} {m[2]}")
        elif m := re.fullmatch(r"(tt|sop|pru|doc|ent)(\d\d)", ficha):
            hallados.append(f"{m[1].upper()}-{m[2]}")
        elif m := re.fullmatch(r"s(1[0-8]|[1-9])", ficha):
            hallados.append(f"S{m[1]}")
        else:
            break
    return hallados


def terminos_en(indice: Indice, texto: str) -> dict[str, dict]:
    """Términos del glosario que usa un texto: citados entre « » o solo mencionados."""
    usados: dict[str, dict] = defaultdict(lambda: {"citas": 0, "menciones": 0})
    for m in RE_CITA_TERMINO.finditer(texto):
        nombre = indice._alias_exacto.get(normalizar(m[1].strip()))
        if nombre:
            usados[nombre]["citas"] += 1
    plano = normalizar(RE_CITA_TERMINO.sub(lambda m: " " * len(m[0]), texto))
    for patron, nombre in indice._alias:
        def tapar(m: re.Match) -> str:
            usados[nombre]["menciones"] += 1
            return " " * len(m[0])
        plano = patron.sub(tapar, plano)
    return dict(usados)


def archivos_del_repo() -> list[Path]:
    try:
        salida = subprocess.run(
            ["git", "ls-files", "--cached", "--others", "--exclude-standard"],
            cwd=RAIZ, capture_output=True, text=True, check=True,
        ).stdout.splitlines()
        rutas = [RAIZ / s for s in salida]
    except (OSError, subprocess.CalledProcessError):
        rutas = [p for p in RAIZ.rglob("*") if "target" not in p.parts and ".git" not in p.parts]
    locales = {".opencode", ".obsidian", ".idea", ".vscode"}
    return sorted(
        p for p in rutas
        if p.suffix in EXTENSIONES and p.is_file() and not locales & set(p.parts)
    )


def texto_de(lineas: list[str], dueno: dict) -> str:
    return "\n".join(lineas[dueno["linea"] - 1 : dueno["hasta"]])


def construir() -> Indice:
    indice = Indice()
    leer_casos(indice)
    leer_supuestos(indice)
    leer_glosario(indice)
    leer_decisiones(indice)
    cache: dict[str, list[str]] = {}

    def lineas_de(archivo: str) -> list[str]:
        if archivo not in cache:
            cache[archivo] = cuerpo(RAIZ / archivo)
        return cache[archivo]

    # Aristas que salen de cada dueño: qué IDs y qué términos cita su texto.
    for ident, nodo in list(indice.ids.items()):
        dueno = nodo["dueno"]
        uc = ident[:4] if ident.startswith("UC") else None
        lineas = lineas_de(dueno["archivo"])
        citados: list[str] = []
        for linea in lineas[dueno["linea"] - 1 : dueno["hasta"]]:
            citados.extend(menciones(linea, uc))
        if nodo["tipo"] != "caso":
            propios = {ident, uc, nodo.get("issue")}
            nodo["cita"] = [c for c in dict.fromkeys(citados) if c not in propios]
        usados = terminos_en(indice, texto_de(lineas, dueno))
        nodo["terminos"] = sorted(usados, key=lambda t: (-usados[t]["citas"], -usados[t]["menciones"], t))
        nodo["_uso"] = usados
        if nodo["tipo"] == "caso":
            for termino in usados:
                indice.terminos[termino]["usado_por"].append(ident)
    for nombre, termino in indice.terminos.items():
        usados = terminos_en(indice, texto_de(lineas_de(termino["archivo"]), termino))
        termino["cita_terminos"] = sorted(t for t in usados if t != nombre)

    # Aristas que llegan: quién menciona cada ID fuera de su texto normativo.
    for ruta in archivos_del_repo():
        archivo = relativo(ruta)
        en_requisitos = archivo.startswith("docs/requisitos/")
        caso = re.match(r"docs/requisitos/casos/(UC0[1-7])-", archivo)
        es_prueba = archivo.startswith("src/test/")
        cubo = "requisitos" if en_requisitos else "docs" if ruta.suffix == ".md" else (
            "pruebas" if es_prueba else "codigo")
        try:
            lineas = lineas_de(archivo)
        except UnicodeDecodeError:
            continue
        for n, linea in enumerate(lineas, 1):
            hallados = menciones(linea, caso[1] if caso else None)
            if es_prueba and (m := RE_PRUEBA.search(linea)):
                hallados = list(dict.fromkeys(hallados + ids_de_prueba(m[1])))
            for ident in hallados:
                conocido = ident in indice.ids and indice.ids[ident]["dueno"]
                if not conocido and tipo_de(ident) in ("caso", "historia", "supuesto", "elemento"):
                    indice.problemas.append(f"referencia sin dueño: {ident} en {archivo}:{n}")
                    continue
                nodo = indice.nodo(ident)
                dueno = nodo["dueno"]
                if dueno and dueno["archivo"] == archivo and dueno["linea"] <= n <= dueno["hasta"]:
                    continue
                nodo["citado_en"][cubo].append(f"{archivo}:{n}")

    for ident, nodo in indice.ids.items():
        if nodo["tipo"] == "sub-issue":
            nodo["historia"] = ident.rsplit("-T", 1)[0]
        if nodo.get("issue"):
            indice.nodo(nodo["issue"])["historia"] = ident
    indice.problemas = sorted(dict.fromkeys(indice.problemas))
    return indice


# ---------------------------------------------------------------- salida


def orden(ident: str):
    return [int(p) if p.isdigit() else p for p in re.split(r"(\d+)", ident)]


def resolver(indice: Indice, consulta: str) -> str | None:
    consulta = consulta.strip()
    if consulta in indice.ids:
        return consulta
    if m0 := re.fullmatch(r"(?:decisi[oó]n\s+|ADR[- ]?)?(\d{4})", consulta, re.I):
        return f"decisión {m0[1]}" if m0[1] in indice.decisiones else None
    hallados = menciones(consulta)
    if consulta in hallados and consulta not in indice.ids:
        return None
    for ident in reversed(hallados):
        if ident in indice.ids:
            return ident
    nombre = indice._alias_exacto.get(normalizar(consulta.strip("«»")))
    return f"«{nombre}»" if nombre else None


def verbatim(dueno: dict) -> str:
    lineas = (RAIZ / dueno["archivo"]).read_text(encoding="utf-8").splitlines()
    return "\n".join(lineas[dueno["linea"] - 1 : dueno["hasta"]]).strip()


def lugar(dueno: dict) -> str:
    return f"{dueno['archivo']}:{dueno['linea']}"


def agrupar(lugares: list[str]) -> list[str]:
    """`a.md:3`, `a.md:9` → `a.md:3,9`."""
    por_archivo: dict[str, list[str]] = defaultdict(list)
    for item in lugares:
        archivo, linea = item.rsplit(":", 1)
        por_archivo[archivo].append(linea)
    return [f"{a}:{','.join(dict.fromkeys(ls))}" for a, ls in por_archivo.items()]


def ficha_termino(indice: Indice, nombre: str) -> None:
    termino = indice.terminos[nombre]
    print(f"«{nombre}»  [término]")
    print(f"  dueño:        {lugar(termino)}")
    if termino["cita_terminos"]:
        print("  cita:         " + ", ".join(f"«{t}»" for t in termino["cita_terminos"]))
    if termino["usado_por"]:
        print("  lo usan:      " + ", ".join(sorted(termino["usado_por"])))


def decisiones_de(indice: Indice, ident: str) -> list[str]:
    return [
        f"{n} ({d['estado'] or 'sin estado'}) → {d['archivo']}"
        for n, d in sorted(indice.decisiones.items()) if ident in d["ids"]
    ]


def ficha_decision(indice: Indice, numero: str) -> None:
    d = indice.decisiones[numero]
    print(f"decisión {numero}  [{d['estado'] or 'sin estado'}]  {d['titulo']}")
    print(f"  archivo:      {d['archivo']}")
    if d["fecha"]:
        print(f"  fecha:        {d['fecha']}")
    origen = "declarados en `srs_ids`" if d["ids_declarados"] else "citados en el texto"
    print(f"  IDs ({origen}): " + ", ".join(sorted(d["ids"], key=orden)))
    for clave, rotulo in (("reemplaza", "reemplaza a"), ("reemplazada_por", "la reemplaza")):
        if d[clave]:
            print(f"  {rotulo + ':':<13} {', '.join(d[clave])}")


def listar_decisiones(indice: Indice) -> None:
    if not indice.decisiones:
        print("No hay decisiones en docs/diseno/decisiones/.")
    for numero, d in sorted(indice.decisiones.items()):
        marca = f"  ← la reemplaza {', '.join(d['reemplazada_por'])}" if d["reemplazada_por"] else ""
        print(f"{numero}  {d['fecha'] or '----------'}  [{d['estado'] or 'sin estado'}]  {d['titulo']}{marca}")


def ficha(indice: Indice, ident: str) -> None:
    if ident.startswith("«"):
        return ficha_termino(indice, ident.strip("«»"))
    if ident.startswith("decisión "):
        return ficha_decision(indice, ident.split()[1])
    nodo = indice.ids[ident]
    print(f"{ident}  [{nodo['tipo']}]" + (f"  {nodo['titulo']}" if nodo["titulo"] else ""))
    if nodo["dueno"]:
        print(f"  dueño:        {lugar(nodo['dueno'])}")
    else:
        print("  dueño:        fuera del SRS (issue del tablero)")
    for clave in ("issue", "historia"):
        if nodo.get(clave):
            print(f"  {clave + ':':<13} {nodo[clave]}")
    if nodo["cita"]:
        print("  cita:         " + ", ".join(nodo["cita"]))
    if nodo["terminos"]:
        if nodo["tipo"] == "caso":
            por_archivo: dict[str, list[str]] = defaultdict(list)
            for t in nodo["terminos"]:
                por_archivo[indice.terminos[t]["archivo"]].append(t)
            usados = len(nodo["terminos"])
            print(f"  términos:     {usados} de {len(indice.terminos)}")
            for archivo in sorted(por_archivo):
                total = sum(1 for t in indice.terminos.values() if t["archivo"] == archivo)
                nombres = ", ".join(f"«{t}»" for t in sorted(por_archivo[archivo]))
                print(f"    {Path(archivo).name} ({len(por_archivo[archivo])}/{total}): {nombres}")
        else:
            print("  términos:     " + ", ".join(f"«{t}»" for t in nodo["terminos"]))
    for linea in decisiones_de(indice, ident):
        print(f"  decisión:     {linea}")
    for cubo, rotulo in (("requisitos", "en el SRS"), ("docs", "en docs"),
                         ("codigo", "en código"), ("pruebas", "en pruebas")):
        lugares = agrupar(nodo["citado_en"][cubo])
        if lugares:
            print(f"  {rotulo + ':':<13} {len(nodo['citado_en'][cubo])} menciones")
            for item in lugares:
                print(f"    {item}")


def contexto(indice: Indice, ident: str) -> None:
    """Texto verbatim del ID, de los IDs que cita y de los términos que usan."""
    if ident.startswith("decisión "):
        sys.exit("Una decisión se lee en su archivo: " + indice.decisiones[ident.split()[1]]["archivo"])
    if ident.startswith("«"):
        nombre = ident.strip("«»")
        termino = indice.terminos[nombre]
        print(f"<!-- {lugar(termino)} -->\n{verbatim(termino)}")
        return
    nodo = indice.ids[ident]
    if not nodo["dueno"]:
        sys.exit(f"{ident} no tiene texto en el SRS: su dueño es un issue del tablero.")
    print(
        "<!-- Extracto derivado de docs/requisitos/ (texto verbatim; manda el archivo dueño).\n"
        "     No reemplaza 01-invariantes.md ni 04-supuestos.md: cárgalos igual. -->\n"
    )
    print(f"## {ident}\n<!-- {lugar(nodo['dueno'])} -->\n{verbatim(nodo['dueno'])}\n")
    terminos = dict.fromkeys(nodo["terminos"])
    supuestos: dict[str, None] = {}
    citados = [c for c in nodo["cita"] if c in indice.ids and indice.ids[c]["dueno"]]
    if nodo["tipo"] != "caso":
        if citados:
            print("## Lo que cita\n")
        for citado in citados:
            otro = indice.ids[citado]
            if otro["tipo"] in ("caso", "historia"):
                print(f"- **{citado}** → {lugar(otro['dueno'])} (no se copia: cárgalo si lo necesitas)")
                continue
            if otro["tipo"] == "supuesto":
                supuestos[citado] = None
                continue
            texto = verbatim(otro["dueno"])
            texto = re.sub(r"^(\d+\. |- )", "", texto)
            print(f"- **{citado}** ({lugar(otro['dueno'])}): {texto}")
            terminos.update(dict.fromkeys(otro["terminos"]))
            supuestos.update(dict.fromkeys(c for c in otro["cita"] if re.fullmatch(r"S\d+", c)))
        print()
    else:
        supuestos.update(dict.fromkeys(c for c in menciones(verbatim(nodo["dueno"])) if re.fullmatch(r"S\d+", c)))
    # Supuestos que nombran este ID o alguno de los que cita.
    del_pack = {ident, *citados}
    for sid, otro in indice.ids.items():
        if otro["tipo"] == "supuesto" and del_pack & set(otro["cita"]):
            supuestos[sid] = None
    if supuestos:
        print("## Supuestos del equipo que lo tocan\n")
        for sid in sorted(supuestos, key=orden):
            terminos.update(dict.fromkeys(indice.ids[sid]["terminos"]))
            print(f"<!-- {lugar(indice.ids[sid]['dueno'])} -->\n{verbatim(indice.ids[sid]['dueno'])}\n")
    vigentes = [l for i in sorted(del_pack) for l in decisiones_de(indice, i)]
    if vigentes:
        print("## Decisiones de diseño que lo tocan (no se copian: cárgalas si aplican)\n")
        for linea in dict.fromkeys(vigentes):
            print(f"- {linea}")
        print()
    if terminos:
        print(f"## Términos del glosario ({len(terminos)} de {len(indice.terminos)})\n")
        for nombre in sorted(terminos):
            termino = indice.terminos[nombre]
            print(f"<!-- {lugar(termino)} -->\n{verbatim(termino)}\n")


def a_json(indice: Indice) -> dict:
    ids = {}
    for ident in sorted(indice.ids, key=orden):
        nodo = {k: v for k, v in indice.ids[ident].items() if not k.startswith("_")}
        ids[ident] = nodo
    return {
        "nota": "Índice derivado por herramientas/indice_ids.py. No es fuente de verdad: "
                "manda docs/requisitos/. No se versiona; regenéralo.",
        "ids": ids,
        "terminos": dict(sorted(indice.terminos.items())),
        "decisiones": dict(sorted(indice.decisiones.items())),
        "problemas": indice.problemas,
    }


def resumen(indice: Indice) -> None:
    conteo: dict[str, int] = defaultdict(int)
    for nodo in indice.ids.values():
        conteo[nodo["tipo"]] += 1
    print("Índice de IDs (derivado; manda docs/requisitos/)")
    for tipo in ("caso", "historia", "paso", "extension", "garantia", "supuesto",
                 "tarea", "sub-issue", "issue"):
        print(f"  {tipo + ':':<11} {conteo[tipo]}")
    print(f"  {'término:':<11} {len(indice.terminos)}")
    print(f"  {'decisión:':<11} {len(indice.decisiones)}")
    sin_prueba = [
        i for i, n in indice.ids.items()
        if n["tipo"] == "historia" and not n["citado_en"]["pruebas"] and not n["citado_en"]["codigo"]
    ]
    print(f"  historias sin mención en código ni pruebas: {len(sin_prueba)} de {conteo['historia']}")
    imprimir_problemas(indice)


def imprimir_problemas(indice: Indice) -> None:
    if not indice.problemas:
        print("Sin problemas: cada referencia tiene dueño y ningún dueño se repite.")
        return
    print(f"Problemas ({len(indice.problemas)}):")
    for problema in indice.problemas:
        print(f"  - {problema}")


def main(argumentos: list[str]) -> int:
    opciones = {a for a in argumentos if a.startswith("--")}
    resto = [a for a in argumentos if not a.startswith("--")]
    desconocidas = opciones - {"--json", "--verificar", "--contexto", "--decisiones", "--ayuda", "--help"}
    if desconocidas or opciones & {"--ayuda", "--help"}:
        print(__doc__)
        return 2 if desconocidas else 0
    indice = construir()
    if "--verificar" in opciones:
        imprimir_problemas(indice)
        return 1 if indice.problemas else 0
    if "--decisiones" in opciones:
        listar_decisiones(indice)
        return 0
    if "--json" in opciones:
        destino = Path(resto[0]).resolve() if resto else SALIDA_JSON
        destino.parent.mkdir(parents=True, exist_ok=True)
        destino.write_text(
            json.dumps(a_json(indice), ensure_ascii=False, indent=1) + "\n", encoding="utf-8"
        )
        print(f"{destino} ({len(indice.ids)} IDs, {len(indice.terminos)} términos)")
        return 0
    if not resto:
        resumen(indice)
        return 0
    ident = resolver(indice, " ".join(resto))
    if not ident:
        print(f"No encuentro «{' '.join(resto)}» en el índice.", file=sys.stderr)
        return 1
    (contexto if "--contexto" in opciones else ficha)(indice, ident)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
