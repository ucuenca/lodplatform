
## Generación de Linked Data a partir de un repositorio digital Dspace ##

En este ejemplo se presentan los pasos y  configuraciones necesarias que han sido aplicadas sobre la plataforma  para la generación y publicación de datos de repositorios digitales siguiendo los principios de Linked Data. En este  caso en particular como fuente se ha tomado el servicio OAI-PMH del repositorio DSPACE de la universidad de Cuenca. 

### Especificación ###
Para la extracción de datos, en este caso en particular que la fuente es un repositorios digital Dspace con servicio OAI-PMH,  se emplea el plugin de lectura  **"OAI-PMH Loader".** Como primer paso se coloca la URL del servicio OAI (http://dspace.ucuenca.edu.ec/oai/request) en el campo *Input URI*. Una vez hecho esto se da click en el boton *Get Formats*, esto despliega los prefijos de los formatos disponibles para extracción de los datos. En este caso se ha seleccionado XOAI. Adicionalmente, se selecciona el path de respuesta desde el cual se tomará los datos. Generalmente se debe tomar la primera opción, que se encuentra marcada en la figura 1. Finalmente, se acepta los cambios y se guarda la configuración.

![Image1Input](./Images/ImagenInput.PNG?style=centerme)
