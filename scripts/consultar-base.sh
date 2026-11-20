#!/bin/sh
# consultar-base.sh · consulta la base de desarrollo de RutaUN (DOC-11, #136).
#
# Uso:
#   scripts/consultar-base.sh "<consulta SQL>" [ruta/de/la/base]
#
# Corre org.h2.tools.Shell sobre data/rutaun.mv.db, con la aplicación detenida.
# Abre la base en solo lectura y usa el jar de H2 que resuelve el wrapper de
# Maven, así que la versión es siempre la del proyecto.
#
# La ruta es opcional y va sin «.mv.db»; si es relativa, se resuelve desde el
# directorio actual. Códigos de salida: 0 si la consulta corrió, 1 si falló la
# conexión o el SQL, 2 si faltó la consulta.
#
# Convención y comandos: docs/proceso/definicion-de-hecho.md, «Cómo se
# demuestra un cambio» (comando B). La contraseña es la vacía por defecto de
# org.h2.tools.Shell; no se pasa -password.

set -u

aqui=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1
raiz=$(CDPATH= cd -- "$aqui/.." && pwd) || exit 1

uso() {
  cat >&2 <<'FIN'
uso: scripts/consultar-base.sh "<consulta SQL>" [ruta/de/la/base]

Consulta la base de desarrollo (data/rutaun) con la aplicación detenida.
La ruta es opcional y va sin .mv.db; si es relativa, se resuelve desde el
directorio actual.
FIN
}

if [ "$#" -lt 1 ]; then
  uso
  exit 2
fi

sql=$1
if [ -z "$sql" ]; then
  uso
  exit 2
fi

if [ "$#" -ge 2 ] && [ -n "$2" ]; then
  base=$2
  case "$base" in
    /*) : ;;
    *) base=$PWD/$base ;;
  esac
  case "$base" in
    *.mv.db) base=${base%.mv.db} ;;
  esac
else
  base=$raiz/data/rutaun
fi

# Sin barras finales, para que la URL no quede con «//».
while [ "$base" != "/" ] && [ "${base%/}" != "$base" ]; do
  base=${base%/}
done

if ! command -v java >/dev/null 2>&1; then
  echo "consultar-base: no encuentro «java» en el PATH. Instala un JDK 21." >&2
  exit 1
fi

if [ ! -f "$base.mv.db" ]; then
  echo "consultar-base: no encuentro $base.mv.db." >&2
  echo "  Arranca primero con el perfil dev:  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev" >&2
  exit 1
fi

temporal=$(mktemp "${TMPDIR:-/tmp}/rutaun-cp.XXXXXX") || exit 1
trap 'rm -f "$temporal"' EXIT INT TERM

if ! (cd "$raiz" && ./mvnw -q -DincludeArtifactIds=h2 -Dmdep.outputFile="$temporal" dependency:build-classpath) >/dev/null 2>&1; then
  echo "consultar-base: no pude resolver el jar de H2 con el wrapper de Maven (./mvnw)." >&2
  exit 1
fi

h2=$(tr -d '\r\n' < "$temporal")
if [ -z "$h2" ]; then
  echo "consultar-base: el wrapper no devolvió la ruta del jar de H2." >&2
  exit 1
fi

salida=$(java -cp "$h2" org.h2.tools.Shell \
  -url "jdbc:h2:file:$base;IFEXISTS=TRUE;ACCESS_MODE_DATA=r" \
  -user sa -sql "$sql" 2>&1)
codigo=$?

if [ "$codigo" -ne 0 ] && printf '%s\n' "$salida" | grep -Eq '\[90020-|Database may be already in use'; then
  echo "consultar-base: la base está abierta por otro proceso." >&2
  echo "  Detén la aplicación: H2 en archivo admite un solo proceso." >&2
  exit 1
fi

printf '%s\n' "$salida"

if [ "$codigo" -ne 0 ]; then
  exit 1
fi

if printf '%s\n' "$salida" | grep -q '^Error:'; then
  exit 1
fi

exit 0
