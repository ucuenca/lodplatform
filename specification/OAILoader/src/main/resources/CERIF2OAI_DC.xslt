<?xml version="1.0"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:oai="http://www.openarchives.org/OAI/2.0/"
        xmlns:m="http://www.lyncode.com/xoai"
        xmlns:oai_cerif="https://www.openaire.eu/cerif-profile/1.1/"
	version="1.0">

<xsl:template match="/record/header">
<dc:identifier><xsl:value-of select="identifier"/> </dc:identifier>

</xsl:template>	
<xsl:template match="/record/metadata/oai_cerif:Publication">
<xsl:for-each select="oai_cerif:Title">
<dc:title> <xsl:value-of select="."/> </dc:title>
</xsl:for-each>

<xsl:for-each select="oai_cerif:DOI">
<dc:doi><xsl:value-of select="."/> </dc:doi>
</xsl:for-each>

<xsl:for-each select="oai_cerif:Handle">
<dc:handle> <xsl:value-of select="."/> </dc:handle>
</xsl:for-each>

<xsl:for-each select="oai_cerif:PartOf">
<dc:PartOf> <xsl:value-of select="oai_cerif:DisplayName"/> </dc:PartOf>
</xsl:for-each>

<xsl:for-each select="oai_cerif:Project">
<dc:Project> <xsl:value-of select="@id" /> | <xsl:value-of select="oai_cerif:Title" /> </dc:Project>
</xsl:for-each>

<xsl:for-each select="oai_cerif:URL">
<dc:URL> <xsl:value-of select="."/>  </dc:URL>
</xsl:for-each>

<xsl:for-each select="oai_cerif:URN">
<dc:URN> <xsl:value-of select="."/> </dc:URN>
</xsl:for-each>

<xsl:for-each select="oai_cerif:Authors/oai_cerif:Author">
<dc:Author> <xsl:value-of select="oai_cerif:Person/@id" />|<xsl:value-of select="oai_cerif:DisplayName"/> </dc:Author>
</xsl:for-each>

<xsl:for-each select="oai_cerif:Keyword">
<dc:Keyword> <xsl:value-of select="." /> </dc:Keyword>
</xsl:for-each>

<xsl:for-each select="oai_cerif:Abstract">
<dc:Abstract> <xsl:value-of select="." /> </dc:Abstract>
</xsl:for-each>



</xsl:template> 


<xsl:template match="/record/metadata/oai_cerif:OrgUnit">
<dc:OrgUnit><xsl:value-of select="@id" /> </dc:OrgUnit>
<dc:Name> <xsl:value-of select="oai_cerif:Name" />  </dc:Name>
<dc:Acro> <xsl:value-of select="oai_cerif:Acro" />  </dc:Acro>

</xsl:template> 


<xsl:template match="/record/metadata/oai_cerif:Person">
<dc:Person> <xsl:value-of select="@id"/>  </dc:Person>
<dc:ORCID> <xsl:value-of select="oai_cerif:ORCID" />  </dc:ORCID>
<dc:ScopusAuthorID> <xsl:value-of select="oai_cerif:ScopusAuthorID" /> </dc:ScopusAuthorID>

<dc:FamilyNames><xsl:value-of select="oai_cerif:PersonName/oai_cerif:FamilyNames" /> </dc:FamilyNames>
<dc:FirstNames> <xsl:value-of select="oai_cerif:PersonName/oai_cerif:FirstNames" /> </dc:FirstNames>
<dc:OrgUnit> <xsl:value-of select="oai_cerif:Affiliation/oai_cerif:OrgUnit/@id" /> </dc:OrgUnit>
<xsl:for-each select="metadata/oai_cerif:Person/oai_cerif:Project">
<dc:Project> <xsl:value-of select="@id" /> | <xsl:value-of select="oai_cerif:Title" /> </dc:Project>
</xsl:for-each>
</xsl:template> 


<xsl:template match="/record/metadata/oai_cerif:Project">
<dc:Project> <xsl:value-of select="@id" />  </dc:Project>
<dc:Title> <xsl:value-of select="oai_cerif:Title" />  </dc:Title>
</xsl:template> 
</xsl:stylesheet>