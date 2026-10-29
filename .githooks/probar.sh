#!/bin/sh
# Pruebas de los hooks de .githooks (ENT-06, #80).
#
#   .githooks/probar.sh
#
# No toca tu repositorio ni tu configuración: los casos de commit-msg usan
# archivos temporales y los de pre-commit, repositorios temporales con un
# mvnw falso. Termina con código 1 si falla algún caso.

hooks=$(cd "$(dirname "$0")" && pwd)
total=0
fallos=0
tmp_msg=$(mktemp)
tmp_repo=""
trap 'rm -f "$tmp_msg"; [ -n "$tmp_repo" ] && rm -rf "$tmp_repo"' EXIT

# Registra un caso: 0 si pasó, 1 si falló; el detalle solo se muestra al fallar.
resultado() {
  total=$((total + 1))
  if [ "$1" -eq 0 ]; then
    printf '  ok     %s\n' "$2"
  else
    fallos=$((fallos + 1))
    printf '  FALLA  %s\n' "$2"
    [ -z "$3" ] || printf '%s\n' "$3" | sed 's/^/           /'
  fi
}

# repetir <texto> <veces>
repetir() {
  r=""
  i=0
  while [ "$i" -lt "$2" ]; do
    r="$r$1"
    i=$((i + 1))
  done
  printf '%s' "$r"
}

# ---------------------------------------------------------------- commit-msg

# msg <0|1 esperado> <descripción> <mensaje (con \n)> [texto que debe aparecer]
msg() {
  printf '%b' "$3" >"$tmp_msg"
  salida=$("$hooks/commit-msg" "$tmp_msg" 2>&1)
  codigo=$?
  ok=0
  [ "$codigo" -eq "$1" ] || ok=1
  if [ "$ok" -eq 0 ] && [ -n "$4" ]; then
    printf '%s' "$salida" | grep -Fq -- "$4" || ok=1
  fi
  resultado "$ok" "$2" "salió con $codigo (esperado $1); salida: $salida"
}

echo "commit-msg · mensajes que deben pasar"
msg 0 "historia con cuerpo y pie" "UC06-HU01: mostrar las reglas del programa por categoría\n\nAgrupa las reglas vigentes y programadas (UC06 paso 4).\n\nRefs #34"
msg 0 "tarea técnica sin cuerpo" "TT-02: modelar la versión del consolidado como raíz de solo agregar\n\nRefs #49"
msg 0 "sub-issue de una historia" "UC01-HU01-T2: crear el formulario del tope\n\nRefs #12"
msg 0 "sub-issue de una tarea técnica" "TT-03-T4: evaluar restricciones por certeza\n\nRefs #50"
msg 0 "ID de soporte, pruebas y entorno" "ENT-06: agregar los hooks de git\n\nRefs #80"
msg 0 "varios issues en el pie" "TT-04: crear el esquema de la base\n\nRefs #51, #55"
msg 0 "pie seguido de Co-Authored-By" "TT-01: actualizar las notas\n\nRefs #78\n\nCo-Authored-By: Persona <persona@example.com>"
msg 0 "líneas de comentario de la plantilla" "TT-01: actualizar las notas\n\nRefs #78\n\n# Plantilla de commit\n# $(repetir x 90)"
msg 0 "verbo con ñ" "TT-03: diseñar el evaluador de restricciones\n\nRefs #50"
msg 0 "verbo reflexivo" "TT-03: asegurarse de que toda regla tenga fuente\n\nRefs #50"
msg 0 "asunto de exactamente 72 caracteres con ñ" "TT-01: actualizar $(repetir ñ 54)\n\nRefs #78"
msg 0 "URL larga en el cuerpo" "DOC-03: documentar las plantillas\n\nVer https://docs.github.com/$(repetir a 80)\n\nRefs #64"
msg 0 "merge generado por git" "Merge branch 'develop' into tt-02-nucleo"
msg 0 "revert generado por git" "Revert \"TT-02: modelar la malla\"\n\nThis reverts commit abc123."

echo "commit-msg · mensajes que deben rechazarse"
msg 1 "sin ID" "arreglos varios\n\nRefs #1" "«<ID>: »"
msg 1 "ID en minúscula" "tt-02: modelar la malla\n\nRefs #49" "«<ID>: »"
msg 1 "ID mal formado" "TT-2: modelar la malla\n\nRefs #49" "«<ID>: »"
msg 1 "resumen con mayúscula inicial" "TT-02: Modelar la malla\n\nRefs #49" "infinitivo"
msg 1 "resumen que no es un verbo en infinitivo" "TT-02: modelado de la malla\n\nRefs #49" "infinitivo"
msg 1 "asunto con punto final" "TT-02: modelar la malla.\n\nRefs #49" "termina en punto"
msg 1 "asunto de 73 caracteres con ñ" "TT-01: actualizar $(repetir ñ 55)\n\nRefs #78" "72 caracteres"
msg 1 "línea 2 sin vaciar" "TT-02: modelar la malla\nsin línea vacía\n\nRefs #49" "línea 2"
msg 1 "línea del cuerpo de 73 caracteres" "TT-02: modelar la malla\n\n$(repetir x 73)\n\nRefs #49" "cuerpo"
msg 1 "sin pie Refs" "TT-02: modelar la malla\n\nCuerpo sin pie." "Refs #n"
msg 1 "Refs sin número" "TT-02: modelar la malla\n\nRefs #" "Refs #n"
msg 1 "Closes en el commit" "TT-02: modelar la malla\n\nRefs #49\nCloses #49" "descripción del PR"
msg 1 "mensaje vacío" "" "«<ID>: »"

# ---------------------------------------------------------------- pre-commit

# nuevo_repo [sin_rama]: repositorio temporal en develop, con un mvnw falso que
# anota sus llamadas en mvnw.llamadas y falla si MVNW_SALIDA=1. Por omisión
# cambia a una rama de trabajo.
nuevo_repo() {
  [ -z "$tmp_repo" ] || rm -rf "$tmp_repo"
  tmp_repo=$(mktemp -d)
  git -C "$tmp_repo" init -q
  git -C "$tmp_repo" symbolic-ref HEAD refs/heads/develop
  git -C "$tmp_repo" config user.name pruebas
  git -C "$tmp_repo" config user.email pruebas@example.invalid
  printf '%s\n' '#!/bin/sh' \
    'echo "$*" >> "$(dirname "$0")/mvnw.llamadas"' \
    '[ -z "$MVNW_MENSAJE" ] || printf "%s\n" "$MVNW_MENSAJE"' \
    '[ "${MVNW_SALIDA:-0}" = 0 ] || { echo "[ERROR] verify simulado"; exit 1; }' >"$tmp_repo/mvnw"
  mkdir -p "$tmp_repo/src/main/java"
  printf 'class A {}\n' >"$tmp_repo/src/main/java/A.java"
  printf 'inicio\n' >"$tmp_repo/LEEME.md"
  git -C "$tmp_repo" add mvnw LEEME.md src
  git -C "$tmp_repo" commit -q -m inicio
  git -C "$tmp_repo" config core.hooksPath "$hooks"
  [ "$1" = sin_rama ] || git -C "$tmp_repo" checkout -q -b rama-de-trabajo
}

# preparar <archivo> [contenido]: crea o cambia el archivo y lo agrega al índice.
preparar() {
  mkdir -p "$tmp_repo/$(dirname "$1")"
  printf '%s\n' "${2:-cambio}" >"$tmp_repo/$1"
  git -C "$tmp_repo" add -f "$1"
}

llamadas() {
  [ -f "$tmp_repo/mvnw.llamadas" ] || { echo 0; return; }
  wc -l <"$tmp_repo/mvnw.llamadas" | tr -d ' '
}

# pc <0|1 esperado> <llamadas a mvnw esperadas> <descripción>
#    [texto que debe aparecer] [texto que no debe aparecer]
pc() {
  salida=$(cd "$tmp_repo" && "$hooks/pre-commit" 2>&1)
  codigo=$?
  n=$(llamadas)
  ok=0
  [ "$codigo" -eq "$1" ] || ok=1
  [ "$n" -eq "$2" ] || ok=1
  if [ "$ok" -eq 0 ] && [ -n "$4" ]; then
    printf '%s' "$salida" | grep -Fq -- "$4" || ok=1
  fi
  if [ "$ok" -eq 0 ] && [ -n "$5" ]; then
    printf '%s' "$salida" | grep -Fq -- "$5" && ok=1
  fi
  resultado "$ok" "$3" "salió con $codigo (esperado $1), mvnw llamado $n vez/veces (esperado $2); salida: $salida"
}

echo "pre-commit · lo que debe pasar"
nuevo_repo
preparar README.md "solo documentación"
pc 0 0 "solo documentación: no corre Maven" "se omite"

nuevo_repo
preparar .github/workflows/ci.yml "name: CI"
pc 0 0 "un workflow no pasa por Maven"

nuevo_repo
preparar .env.example "CLAVE="
pc 0 0 "un .env.example sí puede subirse"

nuevo_repo
preparar src/main/java/A.java "class A { int x; }"
pc 0 1 "cambio en Java: corre verify una vez" "verify en verde"
[ "$(cat "$tmp_repo/mvnw.llamadas")" = "-B verify" ]
resultado $? "verify se invoca como ./mvnw -B verify" "llamada: $(cat "$tmp_repo/mvnw.llamadas")"

nuevo_repo
git -C "$tmp_repo" rm -q src/main/java/A.java
pc 0 1 "borrar un archivo de src también corre verify"

nuevo_repo
preparar pom.xml "<project/>"
pc 0 1 "un cambio en pom.xml corre verify"

nuevo_repo
preparar config/checkstyle/checkstyle.xml "<module/>"
pc 0 1 "un cambio en config/ corre verify"

nuevo_repo
preparar src/main/java/B.java "class B {}"
printf 'class A { int sin_preparar; }\n' >"$tmp_repo/src/main/java/A.java"
pc 0 1 "cambios sin preparar: avisa pero corre" "sin preparar"

echo "pre-commit · lo que debe rechazarse"
nuevo_repo sin_rama
preparar README.md "directo a develop"
pc 1 0 "commit directo en develop" "directos"

nuevo_repo sin_rama
git -C "$tmp_repo" checkout -q -b main
preparar README.md "directo a main"
pc 1 0 "commit directo en main" "directos"

nuevo_repo
preparar src/main/java/A.java "class A { int x; }"
export MVNW_SALIDA=1
pc 1 1 "verify en rojo: no hay commit y se muestra el motivo" "verify simulado"
unset MVNW_SALIDA

nuevo_repo
preparar src/main/java/A.java "class A { int x; }"
export MVNW_SALIDA=1
export MVNW_MENSAJE="[WARNING] src/main/java/A.java:[5,3] (javadoc) MissingJavadocMethod: falta el Javadoc
[WARNING] los plugins no están fijados
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/ruido"
pc 1 1 "verify en rojo: muestra el detalle de Checkstyle y no el ruido de Maven" "MissingJavadocMethod" "Help 1"
unset MVNW_SALIDA MVNW_MENSAJE

nuevo_repo
preparar data/rutaun.mv.db "binario"
pc 1 0 "la base H2 local" "data/rutaun.mv.db"

nuevo_repo
preparar .env "CLAVE=secreta"
pc 1 0 "un .env" ".env"

nuevo_repo
preparar claves/servidor.pem "-----BEGIN-----"
pc 1 0 "una clave privada .pem" "servidor.pem"

# ----------------------------------------------- git commit de punta a punta

echo "git commit · los dos hooks juntos"
nuevo_repo
printf 'class A { int x; }\n' >"$tmp_repo/src/main/java/A.java"
antes=$(git -C "$tmp_repo" rev-parse HEAD)
salida=$(cd "$tmp_repo" && git commit -q -a -m "TT-02: modelar la malla" -m "Refs #49" 2>&1)
codigo=$?
despues=$(git -C "$tmp_repo" rev-parse HEAD)
[ "$codigo" -eq 0 ] && [ "$antes" != "$despues" ] && [ "$(llamadas)" -eq 1 ]
resultado $? "git commit -a con mensaje válido crea el commit y corre verify" "salió con $codigo; salida: $salida"

nuevo_repo
printf 'class A { int x; }\n' >"$tmp_repo/src/main/java/A.java"
antes=$(git -C "$tmp_repo" rev-parse HEAD)
salida=$(cd "$tmp_repo" && git commit -q -a -m "arreglos varios" 2>&1)
codigo=$?
despues=$(git -C "$tmp_repo" rev-parse HEAD)
[ "$codigo" -ne 0 ] && [ "$antes" = "$despues" ] && printf '%s' "$salida" | grep -Fq "Mensaje de commit rechazado"
resultado $? "git commit -a con mensaje inválido no crea el commit" "salió con $codigo; salida: $salida"

nuevo_repo
printf 'class A { int x; }\n' >"$tmp_repo/src/main/java/A.java"
antes=$(git -C "$tmp_repo" rev-parse HEAD)
export MVNW_SALIDA=1
git -C "$tmp_repo" commit -q -a -m "TT-02: modelar la malla" -m "Refs #49" >/dev/null 2>&1
codigo=$?
unset MVNW_SALIDA
despues=$(git -C "$tmp_repo" rev-parse HEAD)
[ "$codigo" -ne 0 ] && [ "$antes" = "$despues" ]
resultado $? "git commit -a con verify en rojo no crea el commit" "salió con $codigo"

nuevo_repo
printf 'class A { int x; }\n' >"$tmp_repo/src/main/java/A.java"
antes=$(git -C "$tmp_repo" rev-parse HEAD)
export MVNW_SALIDA=1
git -C "$tmp_repo" commit -q -a --no-verify -m "wip" >/dev/null 2>&1
codigo=$?
unset MVNW_SALIDA
despues=$(git -C "$tmp_repo" rev-parse HEAD)
[ "$codigo" -eq 0 ] && [ "$antes" != "$despues" ]
resultado $? "git commit --no-verify salta los hooks (la salida de emergencia)" "salió con $codigo"

# ------------------------------------------------------------------- resumen

echo
if [ "$fallos" -eq 0 ]; then
  echo "Todo en verde: $total casos."
  exit 0
fi
echo "$fallos de $total casos fallaron."
exit 1
