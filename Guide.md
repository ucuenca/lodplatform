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

La plataforma de Linked Data esta publicada en un repositorio de software libre (Github) y puede descargase como ZIP  desde la siguiente [enlace](https://github.com/santteegt/lodplatform). Una vez descargada se debe descomprimir en un directorio conocido dentro de la plataforma como, por ejemplo: /home/cedia/LOD

    
