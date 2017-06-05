
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
- Poma Salazar M´onica Eulalia
- Hidalgo David




