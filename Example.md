
## Generación de Linked Data a partir de un repositorio digital Dspace ##

En este ejemplo se presentan los pasos y  configuraciones necesarias que han sido aplicadas sobre la plataforma  para la generación y publicación de datos de repositorios digitales siguiendo los principios de Linked Data. En este  caso en particular como fuente se ha tomado el servicio OAI-PMH del repositorio DSPACE de la universidad de Cuenca. 

### Especificación ###
Para la extracción de datos, en este caso en particular que la fuente es un repositorios digital Dspace con servicio OAI-PMH,  se emplea el plugin de lectura  **"OAI-PMH Loader".** Como primer paso se coloca la URL del servicio OAI (http://dspace.ucuenca.edu.ec/oai/request) en el campo *Input URI*. Una vez hecho esto se da click en el boton *Get Formats*, esto despliega los prefijos de los formatos disponibles para extracción de los datos. En este caso se ha seleccionado XOAI. Adicionalmente, se selecciona el path de respuesta desde el cual se tomará los datos. Generalmente se debe tomar la primera opción, que se encuentra marcada en la figura 1. Finalmente, se acepta los cambios y se guarda la configuración.

![Image1Input](./Images/ImagenInput.PNG?style=centerme)


### Modelamiento ###
Para esta etapa se utiliza el plugin **"Get Properties OWL"** que se encarga de cargar los vocabularios de las ontologías. En este caso se han seleccionado varias ontologias que permiten describir semánticamente los diferentes recursos que dispone lo repositorios. Entre estos se encuentran:

- bibo: Empleado para describir recursos bibliográficos. 
- foaf: Utilizado para describir personas, objetos  y propiedades asociadas.
- dcterms: Vocabulario empleado para describir algunas propiedades asociadas a documentos.
- bibtex: Dispone de algunos vocabularios específicos enfocados en  describir referencias de recursos bibliográficos. 
- rdda: Contiene vocabulario especializado para describir elementos relacionados con bibliotecas. 

Para ingresar los vocabularios de las ontologías dentro del plugin, se puede optar por dos alternativas: usar un prefijo de la ontologia o cargar un archivo con el modelo.  En este caso se ha optado por la manera mas sencilla que es   ingresar el nombre o prefijo en el campo de "Input Ontology URL or the name of prefix" y cargar los datos mediante el botón "Add URI".   Una vez se ha realizado el proceso con todas las ontologias disponibles se puede precargar los datos con el boton "Pre-cath data" para ser utilizados posteriormente.

![Image1Input](./Images/ucuencaonto.png?style=centerme)


### Generación ###
Dentro de la fase de generación se dispone de varias actividades, que pueden ser llevadas a cabo mediante los plugins propios del framework junto con algunos plugins nativos de Kettle. 

#### Limpieza ####
El proceso de limpieza busca mejorar la estandarización y calidad de los datos previo a la generación RDF. Para realizar este proceso los datos deben pasar previamente por un proceso de análisis que permita distinguir algunos problemas de estandarización asi como  errores. En el repositorio mencionado se ha logrado distinguir por ejemplo errores en los autores tal como se detalla a continuación:

Datos adicionales no correspondientes a los nombre propios de autores, por ejemplo:
- Recalde Moreno, Dr. Celso
- Torres Arroba, Fernando Javier Ph.D

Dos nombres pertenecientes a diferentes autores en un solo campo:
- Castro Muñoz, Celia María;Chang Gómez, José Vicente
- Rivas Tarazona, RossanaOsorio Llanos, Ana Cecilia

Nombres completos sin separador entre nombres y apellidos.
- Poma Salazar Mónica Eulalia
- Hidalgo David

Para idiomas se han encontrado dos representaciones diferentes para el idioma español, tal como se muestra a continuación:
- es
- esp


Para solucionar este y otros problemas se requieren flujos de transformación que acondicionen y corrigan los problemas encontrados en los datos, para esto fueron utilizados la gran variedad de plugins nativos de Kettle. Para mayor información acerca de estos procesos  revisar la  documentacion de Pentaho Data Integration. Para este ejemplo en particular revisar transformación de ejemplo.

![Image1Input](./Images/TrasnfLimpieza.PNG?style=centerme)


Los datos una vez se han pasado por los flujos de limpieza, deben ser almacenados temporalmente en una base de datos para evitar sobrecargar la memoria. Para realizar este proceso se dispone del plugin "Data Pre-Catching" , al cual debe enviarse los datos de forma normalizada con los siguientes campos:
- Id Record: Identificador del recurso. Ejemplo "oai:localhost:123456789/333" para documento.
- Field: Indica el tipo de dato  que contiene el campo Data. Ejemplo "Título" 
- Data: Valor de interes. Ejemplo. "Enriquecimiento Semántico..."

El plugin al ser insertado en la transformación automáticamente se cargara con los datos por defecto, por lo que no es necesario realizar algun cambio sobre la configuración para su funcionamiento. La base de datos con los datos y otras configuraciones  se crearan en la carpeta del usuario.

![Image1Input](./Images/UCUEdatapre.png?style=centerme)

#### Conversión ####
Una vez se dispone de los datos y el vocabulario cargado, se procede a generar las reglas de asociación que permitiran generar  la descripción en RDF. Para realizar esta actividad se utiliza el plugin "Ontology & Data Mapping".  Dentro del plugin existen configuraciones generales tales, como:


![Image1Input](./Images/UCUEMAP1.png?style=centerme)


- Ontologies Step:  Nombre asignado al plugin de carga de vocabulario. Por defecto "Get Properties OWL".
- Data Step: Nombre asignado al plugin de cache. Por defecto "Data Precatching".
- Data Base URI: URI base para todos los recursos. Es recomendable que esta dirección apunte a un servidor accesible y en el cual se puede levantar un servicio para visualizar los recursos. Para este caso se ha incluido la fuente de origen del recurso para facilitar su identificación.
- Output Directory: Salida del archivo de reglas.


La definición de las reglas entre los vocabularios y los datos constan de 3 apartados:
 **Clasificación**
En este apartado se declaran los recursos existentes en los datos. Por ejemplo Documentos, Personas, Colecciones, etc. Para generar las reglas de asociaciación este apartado dispone de varios campos. 
- ID: Identificador de la regla de clasificación. Puede ser empleado  para identificar a los recursos en los siguientes apartados.
- Ontology y  Entity: Ontologia y vocabulario con el cual se identificara al recurso.
- Relative URI: URI relativa que identifica al tipo de recurso y se sumara a la URI base.
- URI Field ID: Campo del cual se extraera el identificador  unico del recurso. Dentro de este campo se pueden realizar operaciones tales como substring() para tomar unicamente parte de un campo como clave o upper() para pasar el campo clave a mayúsculas. Con este identificador se completara la URI del recurso.
- Data Field y  Data Value: Campo y valor que se buscara en cada una de las filas de los datos y por la cual se aplicara dicha regla. Dentro del campo data value se puede usar operadores como "%" que indican que pudede ir  cualquier valor despues del patron previamente definido.

![Image1Input](./Images/UCUEClassification.png?style=centerme)

En el primer caso (C001) por ejemplo cuando se encuentre los siguientes datos:

| Id Record | Field | Data  |
|---------|----------|--------|
|oai:localhost:123456789/141   | Identifier   |   oai:localhost:123456789/141      |

Mediante la configuración generada se crea la siguiente tripleta:

\<http://190.15.141.66:8899/ucuenca/recurso/141> a \<http://purl.org/ontology/bibo/Thesis> 

Para el caso de personas (C012), dada la siguiente entrada:

| Id Record | Field | Data    |  
|---------|----------|--------|
| oai:localhost:123456789/141 | dc/contributor/author/none   |   Brito Rivas, Mauricio Rodrigo     |


Se genera la siguiente tripleta:

\<http://190.15.141.66:8899/ucuenca/contribuyente/BRITO_RIVAS__MAURICIO_RODRIGO> a \<http://xmlns.com/foaf/0.1/Person>

**Anotación**

Este apartado permite asociar propiedades de tipo literal a los recursos y dispone de algunos campos similares a los presentados anteriormente:

- ID: Identificador de la regla de anotación. 
- Entity Class ID: Permite referenciar al identificador de  una regla de clasificación. En otras palabras permite referenciar al recursos declarado.
- Ontology y  Property: Ontologia y vocabulario con el cual se identificara la relación de propiedad.
- Extraction Field: Campo del cual se extraera el valor de la propiedad generalmente Data.
- Data Field y  Data Value: Campo y valor que se buscara en cada una de las filas de los datos y por la cual se aplicara dicha regla en la generación de una relación de propiedad.
- Data Type: Permite seleccionar el tipo de campo con el cual se identificara a la propiedad generada.



![Image1Input](./Images/UCUEAnotation.png?style=centerme)

En  la regla de asociación (A001) se define la asignación de una propiedad de título al documento declarado en la regla (C001) por lo tanto dado una entrada:

| Id Record | Field | Data    |  
|---------|----------|--------|
| oai:localhost:123456789/141 |  dc/title/es_ES     |   Diseño e implementación de servicios especializados para el portal ...      |

Se obtendra la siguiente tripleta:

\<http://190.15.141.66:8899/ucuenca/recurso/141> \<http://purl.org/dc/terms/title> "Diseño e implementación de servicios especializados para el portal del Centro de Documentación Juan Bautista Vázquez"^^\<http://www.w3.org/2001/XMLSchema#string>


![Image1Input](./Images/UCUEREL.png?style=centerme)




