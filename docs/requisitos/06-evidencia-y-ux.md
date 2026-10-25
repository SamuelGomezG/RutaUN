---
title: "Evidencia · Acta ZEUS-02"
doc_type: "evidencia"
srs_ids:
  - §13
depends_on:
  - 07-lagunas.md
  - 00-contexto.md
---
# 13. Anexo · Acta de la segunda entrevista (ZEUS-02)

> **ADVERTENCIA:** evidencia del acta ZEUS-02; **no es requisito de comportamiento**. Las pautas de UI acordadas a partir de esta entrevista están en `07-lagunas.md` §14.2 y **sí** respetan el diseño de la interfaz.

*(Misma plantilla y mismas exigencias que el acta de la Entrega 1, con identificador consecutivo. Los datos personales de la persona entrevistada y de su acompañante se omiten a propósito; se conserva su rol.)*

## 13.1 Datos de la entrevista
- **Equipo / acta / fecha / duración:** ZEUS · **ZEUS-02** · 6/10/2026 · ~26 minutos.
- **Persona entrevistada:** estudiante de Ingeniería de Sistemas y Computación (UNAL Sede Bogotá, equivalente a 9.º semestre) que cursa doble titulación con Diseño Industrial (equivalente a 2.º semestre). Trabaja además de estudiar; este semestre cursa 15 créditos entre ambos programas. Estuvo presente también un amigo suyo, que participó al final para dar su punto de vista y aportar sugerencias sobre la presentación del prototipo.
- **Cargo o relación con el problema:** estudiante que debe planear y cuadrar manualmente su carga entre dos mallas curriculares distintas, incluyendo sobrecupos y homologaciones entre programas.

## 13.2 Retroalimentación de los casos de uso definidos y el prototipo mostrado

*(«Descripción de la recepción y punto de vista del entrevistado con respecto a los casos de uso y prototipo que le fueron explicados durante el encuentro». Las citas son textuales del acta; «el entrevistado» y «el acompañante» sustituyen los nombres.)*

| Lo que dijo la persona | Lo que interpretamos nosotros |
|---|---|
| "Siento que los casos de uso si engloban lo que hablamos en la primera entrevista" | Los casos de uso definidos le parecen adecuados y considera que se ajustan a los problemas que fueron comentados en la primera entrevista, plasmada en el acta ZEUS-01. |
| "Me pregunto si es posible que para los estudiantes de doble titulación se muestren los dos avances, el de la primera carrera y si homologaran las materia qué avance tendría en la otra carrera, uno va metiendo materias de la primera carrera y no sabe cuando le homologuen en la segunda" | Al parecer los miembros del equipo fallaron al momento de explicar con claridad al entrevistado el alcance del proyecto y lo que se dejó fuera. **Manejar los planes de dos carreras diferentes de forma simultánea está fuera del alcance del proyecto, de momento.** |
| Sobre el prototipo: "¿Estos son los colores que le van a dejar?...A nivel visual es demasiada información de golpe, de pronto permitirle a la persona ver solo la carrera y se despliegue toda la información al darle click, porque eso satura un poco a la persona" | El entrevistado ofreció una retroalimentación sobre el diseño visual del prototipo, sobre cómo tiene demasiada información y la sensación que genera al usuario. Aunque este es un prototipo de baja fidelidad igual es importante tener en cuenta el consejo para el desarrollo del producto final. |
| Sobre los casos de uso UC-05 y UC-06 referentes a la aplicación de las restricciones de las materias y la información sobre normativas: "Saber que cosas de la normativa se aplican realmente al SIA...verificar que si en la normativa dice algo se aplique en el SIA, revisar que tan real es lo que dice la normativa " | El entrevistado ofrece una nueva fuente de conflicto que el equipo no había contemplado, al parecer **el SIA no siempre implementa las restricciones de las materias** y permite a los estudiantes inscribir cursos para los cuales no se tienen cumplidos los requisitos. Por el momento la atención a este nuevo problema está fuera del alcance del proyecto, pues no se cuenta con contacto directo con los responsables del SIA, pero es un buen tema de discusión para futuras entregas. |
| (Acompañante) "La página es cero intuitiva, tengan en cuenta que un prototipo debe tener ciertas capacidades que se van a analizar con la recepción del público, el prototipo debería tener cosas que ya tendría el producto final. Es demasiada información, es agobiante" | El acompañante aporta una crítica que va en línea de lo que ya había comentado el entrevistado, las vistas del prototipo contienen demasiada información y esto "agobia" al usuario; para él, el prototipo debería haber considerado este problema a pesar de ser desechable y de baja fidelidad. |
| (Acompañante) "¿Como definirían la carga de una materia, por opiniones de estudiantes, por experiencias de otros?...Hay que tener en cuenta que las materias tienen una carga a partir de los crédito, pero dependiendo del profesor se va a tener una carga diferente así sea la misma materia" | Al parecer el acompañante mal interpretó algo de lo que dijeron los miembros del equipo, pensó que al hablar de carga académica nos referíamos al trabajo o esfuerzo específico que requiere cursar una materia con un profesor específico. Hacer esta estimación es algo que se sale del alcance del proyecto, pues **para RutaUN la carga académica se relaciona únicamente con el número de créditos de una materia.** |
| "No se si han escuchado de los tooltips, esas herramientas que uno le pone al usuario para que se familiarice, un círculo al que si la persona pone el cursor encima, muestra información" | El entrevistado ofrece un consejo que permitirá en un futuro hacer la interfaz más clara para el usuario, **implementar ToolTips en el producto final** para hacer más claro el proceso de registrar el avance del estudiante y evitar confusiones y errores al momento de ingresar los datos. |
| "Podrían hacer que el formulario se va habilitando en el orden que el usuario debería llenarlo, al principio mostrar solo la primera pregunta y al hacer click van apareciendo las demás" | Adicionalmente, el entrevistado propone que el proceso para ingresar los datos sea guiado mediante el diseño de la interfaz, **ir habilitando campos para ingresar información conforme los vaya ingresando el usuario**, así se evita que el usuario se pierda y por la frustración y confusión termine tomando la decisión de no utilizar el producto. |

> El acta original salta de la sección 2 a la 5: las secciones 3 y 4 de la plantilla no aparecen en el PDF entregado.

## 13.3 Términos del oficio
*(Palabras que usó la persona y que tuvieron que preguntar qué significaban.)*
- **Producto MVP:** Producto Viable Mínimo o en inglés Minimum Viable Product, es un producto diseñado con las mínimas características y funcionalidades que satisfagan las necesidades del usuario y que permiten una retroalimentación para un desarrollo futuro.
- **ToolTips:** Un ToolTip es un elemento emergente que contiene información adicional sobre otro control u objeto. Las ToolTips se muestran automáticamente cuando el usuario pone el foco en el control asociado, lo presiona y mantiene, o pasa el puntero sobre él.

## 13.4 Qué quedó ambiguo
- No quedó claro si el problema de normativas vigentes que no se aplican al SIA, por ejemplo una materia que se puede inscribir a pesar de no cumplir con los requisitos estipulados, se aplica a la carrera de Ingeniería de sistemas, la de diseño técnico *(sic; probablemente Diseño Industrial)*, o a las dos. Tampoco se aclaró en ningún momento si esto es un problema generalizado o es algo anecdótico que ocurre con unas pocas materias.
- En el momento en que el entrevistado sugiere mostrar dos avances, uno para la primera carrera y otro para el avance que tendría la segunda carrera en el caso de que homologuen las materias, surge la duda de si al entrevistado le queda claro que el alcance actual se remite a trabajar una carrera y que por el momento la opción de registrar el avance de dos carreras simultáneas no está contemplado.

## 13.5 Preguntas para la próxima entrevista
1. ¿Cuando te encontraste con casos donde el SIA no aplica las restricciones correspondientes a las materias, pasó con materias de sistemas, diseño industrial o ambas?
2. ¿Cuántas veces te has encontrado con casos donde el SIA no aplica las restricciones, consideras que es un tema generalizado o son casos particulares?
3. ¿Eres consciente de que manejar dos carreras simultáneas está fuera del alcance de este proyecto?
4. ¿Sigues interesado en que se continúe con el desarrollo del proyecto a pesar de que no se ajuste totalmente a tu situación como estudiante de doble grado?

## Ver también

- Pautas de UX: [07-lagunas.md](07-lagunas.md) §14.2.
