# Función compartida por los hooks pre-commit y pre-push (ENT-06, #80).
# No es un hook: git no la ejecuta, y los hooks la cargan con
#   . "$aqui/verificar.sh"
# Corre ./mvnw verify en el directorio actual, que el hook debe haber fijado
# en la raíz del repositorio.
#
#   correr_verify <commit|push>
#
# Devuelve 0 si verify termina en verde y 1 si falla, tras mostrar lo esencial
# del motivo.

correr_verify() {
  hook="pre-$1"
  case "$1" in
    commit)
      consecuencia="no se crea el commit"
      salto="git commit --no-verify"
      tras_formato="vuelve a preparar los archivos con git add"
      ;;
    *)
      consecuencia="no se empuja"
      salto="git push --no-verify"
      tras_formato="confirma los archivos corregidos (un commit nuevo o git commit --amend)"
      ;;
  esac

  salida=$(mktemp "${TMPDIR:-/tmp}/rutaun-verify.XXXXXX") || salida=/tmp/rutaun-verify.log
  echo "$hook: ./mvnw verify (formato, estilo, arquitectura y pruebas)..."
  inicio=$(date +%s)
  if sh ./mvnw -B verify >"$salida" 2>&1; then
    segundos=$(($(date +%s) - inicio))
    echo "$hook: verify en verde (${segundos} s)."
    if [ "$1" = commit ] && [ "$segundos" -gt 30 ]; then
      echo "  aviso: pasó de 30 s; conviene dejar verify solo en pre-push y quitarlo de pre-commit (ENT-06)."
    fi
    rm -f "$salida"
    return 0
  fi

  echo "$hook: verify falló; $consecuencia. Lo esencial:" >&2
  # Maven marca los errores con [ERROR], pero Checkstyle da el detalle de cada
  # violación en líneas [WARNING] que nombran el archivo .java.
  esencial=$(grep -E '^\[ERROR\]|^\[WARNING\] [^ ]*\.java' "$salida" \
    | grep -Ev '^\[ERROR\] *$|^\[ERROR\] \[Help|To see the full stack trace|Re-run Maven using|For more information')
  if [ -n "$esencial" ]; then
    printf '%s\n' "$esencial" | head -30 | sed 's/^/  /' >&2
  else
    tail -30 "$salida" | sed 's/^/  /' >&2
  fi
  if grep -q 'spotless:apply' "$salida"; then
    echo "  Formato: ./mvnw spotless:apply y $tras_formato." >&2
  fi
  echo "  Salida completa: $salida" >&2
  echo "  (En una emergencia, $salto salta los hooks.)" >&2
  return 1
}
