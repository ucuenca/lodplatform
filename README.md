
# LOD-GF #

Linked Open Data Platform: Solution to accomplish the life cycle management for publishing Linked Data on the Web.

## Introducción ##

Con el avanzar del tiempo un número  mayor de empresas e instituciones se han ido sumando a la iniciativa de Linked Open Data, atraídos  por las múltiples ventajas que esta tecnología aporta tanto en la reutilización, integración  y  el aprovechamiento superior de la información. Sin embargo, aunque esta tecnología ha ido creciendo constantemente, aun debe superar serios factores que afectan su adopción generalizada. Dentro de estos factores se encuentran la necesidad de conocimiento avanzado acerca de tecnologías semánticas, así como la falta de herramientas que soporten el proceso de generación y publicación de Linked Data. Para afrontar esta problemática ha surgido LOD-GF,  el cual es un framework basado en [Pentaho Data Integration](http://community.pentaho.com/projects/data-integration/) que brinda un entorno unificado para el soporte de cada una de las fases de la  metodología de publicación de Linked Open Data.
![ImageLOD](./Images/MLOD.png?style=centerme)



Para soportar cada una de estas fases el framework emplea plugins (modulos de procesamiento especializados) tanto nativos de este entorno  como desarrollados para brindar  un marco de trabajo gráfico y flexible, con el cual los usuarios puedan convertir sus datos sobre un amplio dominio de fuentes o  formatos a datos enlazados de calidad.
![ImageARQLOD](./Images/MPLUG.png?style=centerme)
                                                


LOD-GF es  amigable al usuario gracias a su interfaz gráfica y sus mecánicas *drag&drop* heredadas de Pentaho Data Integration (PDI). 
Para que el framework  pueda soportar el proceso de generación y publicación de LOD, adicionalmente a los [plugins](http://wiki.pentaho.com/display/EAI/Pentaho+Data+Integration+Steps)  nativos que cuenta PDI , se han generado  plugins especializados haciendo que juntos satisfagan las necesidades presentadas en cada una de las fases de la metodología. 
 ![ImageFramP](./Images/LODGENERALV.png?style=centerme)










** Build Status **

[ ![Codeship Status for lod/LODPlatform](https://codeship.com/projects/f70d1860-b628-0132-afb1-32912015c090/status?branch=master)](https://codeship.com/projects/70968)


The project guarantees a continuous Integration using Codeship.
