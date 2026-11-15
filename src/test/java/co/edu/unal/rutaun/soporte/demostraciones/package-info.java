/**
 * Demostraciones del dominio (segundo criterio de la Definición de Hecho).
 *
 * <p>Una demostración muestra el comportamiento de un cambio de dominio que no tiene pantalla, de
 * modo que quien revisa lo vea sin leer las pruebas. Se ejecuta con el JDK y las clases del
 * proyecto: {@code ./mvnw -q test-compile} y {@code java -cp target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.<Clase>}. En Windows, el classpath va entre comillas:
 * {@code java -cp "target/classes;target/test-classes" …}; el {@code ;} separa comandos en
 * PowerShell. El comando no cambia de directorio, así que se corre desde la raíz del repositorio.
 *
 * <p>Forma: una clase {@code DemostracionDe<Tema>}, {@code final}, con constructor privado y un
 * {@code main} que imprime en la salida estándar. Su javadoc dice qué imprime y qué IDs del SRS
 * muestra.
 *
 * <p>Límites: sin Spring y sin base de datos, porque el comando pone en el classpath solo {@code
 * target/classes} y {@code target/test-classes}. Un cambio que necesite librerías o el contexto de
 * Spring se demuestra con la aplicación o con una prueba de aceptación, no aquí. Ejercita las
 * clases de producción del cambio; los dobles de {@code soporte/} solo sustituyen a los
 * colaboradores que el cambio no toca. Los datos son sintéticos: nunca datos personales (regla de
 * oro 7).
 *
 * <p>Una por tema: cada PR agrega a la demostración de su tema lo que cambia, en lugar de crear una
 * clase por PR. No es una prueba: Surefire no la ejecuta, porque su nombre no termina en {@code
 * Test}, y no reemplaza a las pruebas, que son el tercer criterio de la Definición de Hecho.
 */
package co.edu.unal.rutaun.soporte.demostraciones;
