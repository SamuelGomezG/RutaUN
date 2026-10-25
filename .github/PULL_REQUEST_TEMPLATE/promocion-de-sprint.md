<!-- PR de promoción: `develop` → `main`, una vez por sprint y solo si la demostración lo dio por completo y satisfactorio.
     Título: «Sprint N». Se fusiona con «Create a merge commit» (nunca Rebase ni Squash).
     Después de fusionar: etiquetar `sprint-N` y alinear `develop` (`git switch develop && git merge --ff-only main`).
     Para abrirlo con esta plantilla: gh pr create --base main --head develop --title "Sprint N" --body-file .github/PULL_REQUEST_TEMPLATE/promocion-de-sprint.md -->

## Sprint

N · del DD de mes al DD de mes de AAAA. Demostración: DD de mes.

## Qué se mostró en la demostración

<!-- Los flujos de punta a punta que se ejecutaron y el resultado que se vio, frente a la meta del sprint (README, «Meta del sprint»). -->

-

## Issues terminados

<!-- Uno por línea, con su número: los que se comprometieron y los adicionales que alcanzaron. -->

- #

## Comprometidos que no se terminaron

<!-- Cada uno con a dónde pasa (siguiente sprint o backlog). Si no hay, escribe «Ninguno». -->

## Antes de fusionar

- [ ] `develop` tiene la integración continua en verde.
- [ ] Los elementos comprometidos cumplen la [Definición de Hecho](https://github.com/SamuelGomezG/RutaUN/blob/develop/docs/proceso/definicion-de-hecho.md).
- [ ] Lo aprobó una persona distinta de quien abrió el PR.
- [ ] El README refleja lo que hoy funciona (los pasos de «Abrir y ejecutar» se ejecutaron en un clon limpio).
