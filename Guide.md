## Guía de instalación de la plataforma para la generación de Linked Data ##

A continuación se resume el proceso de instalación del framework de generación de LOD usado sobre Pentaho Data Integration. Para esto se ha separado el proceso en dos etapas: 

- Instalación y configuración de tecnologias asociadas (Pre-requisitos)
- Descarga y instalación del framework

### Pre-requisitos ###

La siguiente descripción de la  instalación tiene en consideración que se dispone de un computador con sistema operativo Linux (Ubuntu 14.04+) con  acceso a internet.

#### Instalación de Java ####

En caso de no tener instalada la plataforma Java en el sistema, se debe proceder a su instalación ya que todos los componentes de la plataforma utilizan esta plataforma. La versión recomendada es el JDK 7 (oracle).  Esta plataforma puede instalarse utilizando los siguientes comandos.

:::bash
    sudo add-apt-repository ppa:webupd8team/java 
    sudo apt-get update 
    sudo apt-get install oracle-java7-installer
    
 
#### Descarga Pentaho ####

La plataforma de Linked Data se desarrolló y probo usando la versión 5.1 de Pentaho Data Integration (Kettle), es por esto que se recomienda usar la misma versión. Este software se puede descargar de forma gratuita desde la siguiente [URL](https://sourceforge.net/projects/pentaho/files/Data%20Integration/5.1/pdi-ce-5.1.0.0-752.zip/download).  La descarga consiste en un archivo zip que debe ser descomprimido en una ruta conocida del sistema operativo, por ejemplo: /home/cedia/PDI. 
Opcionalmente, se puede agregar un enlace (acceso directo) al archivo spoon.sh ya que este es el ejecutable principal con el cual se puede iniciar la aplicación.

#### Instalación de Maven ####
La plataforma fue desarrollada usando el utilitario de desarrollo Maven, el cual facilita el manejo de dependencias e instalación de los Plugins. La versión recomendada a ser usada es la 3.0, la cual puede ser descargada e instalada usando los siguientes comandos.

:::bash
    sudo apt-get install maven
    
### Descarga e instalación del Framework ###

La plataforma de Linked Data esta publicada en un repositorio de software libre (Github) y puede descargase como ZIP  desde la siguiente [enlace](https://github.com/santteegt/lodplatform). Una vez descargada se debe descomprimir en un directorio de fácil acceso como el home.

#### Configuración e instalación ####

Una vez descargado el código fuente se debe especificar el directorio donde deben ser instalados los plugins, esto se hacen en el archivo pom.xml del directorio descargado. El archivo pom.xml  es un archivo XML que contiene configuraciones de compilación para el proyecto, por lo que aquí se debe definir  la ruta de salida para los archivos compilados. Esta configuración se lo realiza en la siguiente ruta XML  (/project/properties/pdiDirectory) como se muestra en el ejemplo. 
:::xml

    <properties>
    <pdiDirectory>/home/cedia/PDI/data-integration</pdiDirectory>
    <ucuenca.lod.version> ${project.version}</ucuenca.lod.version>



La ruta de salida debe apuntar a la raiz del directorio de Pentaho data integration para que los plugins una vez compilados se encuentren disponibles para su uso por el framework. Una vez configurada la ruta de salida se puede proceder a compilar e instalar los plugins, esto se realiza usando Maven con los siguientes comandos en consola dentro de la carpeta raiz de los plugins framework descargado  (/lodplatform-master).
:::bash
       
       mvn clean install

Este comando compila el código fuente y agrega los ejecutables a la carpeta de Pentaho. Es necesario ejecutarlo solamente una vez, para posteriores ejecuciones de la plataforma solamente se requiere iniciar Pentaho normalmente.

#### Ejecución de la plataforma ####

Una vez instalados los plugins para ejecutar la plataforma se tendrá que abrir el archivo ejecutable spoon.sh de la carpeta de Pentaho. Ejemplo (/home/cedia/PDI/data-integration/spoon.sh).  Tambien es posible ejecutar la aplicación con el siguiente comando (dentro de la carpeta de Pentaho).

:::bash
    
     ./spoon.sh

Al abrir esta aplicación se presentará la interfaz gráfica de Pentaho Data Integration con los plugings del framework de Linked Data como se muestra en la siguiente imagen.


![Imagen](./Images/guidelod.PNG?style=centerme)

    
