

### Componentes ###

####  OAI-PMH Loader ####
Este plugin permite la lectura de repositorios digitales que dispongan del servicio OAI-PMH.

![OAIConfig](./Images/OAIconfig.png?style=centerme)


- Input URI: Ruta de acceso al servicio OAI-PMH perteneciente al repositorio.
- Prefix: Específicación del formato de lectura que puede ser extraído con el servicio, por ejemplo OAI-DC o XOAI.
- GetXPATH: Ruta desde la cual se empezara a leer la información.


#### MARC Input #### 
Plugin para la lectura de registros  desde ficheros en formato MARC 21.

![MARCConfig](./Images/marc21config.png?style=centerme)

- Batch: Opción que permite la lectura de varios  archivos dentro de un directorio.
- Name of the marcfile: Ubicación del archivo con extensión Marc 21 para su lectura.
- Generate Marc XML: Esta opción permite exportar  la información del fichero MARC a XML.
- Field to load separate with @: Permite definir los campos marc 21,  que seran extraídos del fichero. Para definir varios campos se requiere separarlos mediante @.

#### GET PROPERTIES OWL ####
Carga los modelos ontológicos y su  vocabulario 

![MARCConfig](./Images/ontologyload.png?style=centerme)

- Input URL or the Name of Prefix: En este campo se puede ingresar la URL completa de la ontología o su prefijo. Para verificar el prefijo de una ontologia revisar la página [prefix](http://prefix.cc/)
- Add URI: Mediante este botón se puede cargar la ontología definida en el campo anterior.
- Load File: Despliega un ventana en la cual se puede ingresar la ruta del archivo que contiene la ontología.
- Delete Record: Permite borrar una ontología seleccionada en la grilla inferior.
- Precatch Data: Con esta opción se puede precargar el vocabulario en el framework y facilitar el acceso a los atributos de la ontología  en el componente **Ontology&DataMapping**

#### Data Pretcaching ####
Este plugin es empleado para guardar temporalmente los datos luego del proceso de limpieza y facilitar su lectura desde los plugins de Mapping (**Ontology&DataMapping**) y generación (**RDF GENERATION**)  de RDF.

![MARCConfig](./Images/Datapretconfig.png?style=centerme)

- DB Connection URI: Ruta  de la base de datos **h2** que almacenara los datos.
- DB Table Name: Nombre de la tabla que almacenara los datos.

\*En caso de no realizar cambios, se tomara una configuración por defecto.

#### Ontology & Data Mapping ####
Permite definir los reglas de asociación de los datos de las fuentes con el vocabulario ontológico seleccionado.

![MARCConfig](./Images/mappingconfig.png?style=centerme)


#### RDF GENERATION ####

![MARCConfig](./Images/generatorconfig.png?style=centerme)

- Data base URI: URI absoluta para los recursos.
- RDF output File: Ruta de salida con el archivo RDF.
- RDF output Format: Formato especco en el cual se generara el RDF. (Disponible XML y TTL).
- Retrieve DB connection from input step: Con este boton podemos recuperar las conguraciones de base de datos realizadas en el plugin \Data Precatching".


