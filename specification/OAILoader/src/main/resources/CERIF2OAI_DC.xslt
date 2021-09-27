<?xml version="1.0"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                                     xmlns:dc="http://purl.org/dc/elements/1.1/"
                                     xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                                     xmlns:m="http://www.lyncode.com/xoai"
                                     xmlns:oai_cerif="https://www.openaire.eu/cerif-profile/1.1/"
                                     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                                     version="1.0">

    <xsl:output method="xml" indent="yes"/>
    
    
    <xsl:template match="/">
        <oai_dc:dc>
            <xsl:for-each select="oai_cerif:Person">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="oai_cerif:Project">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="oai_cerif:OrgUnit">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="oai_cerif:Publication">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
        </oai_dc:dc>
    </xsl:template>
    
    
    
    <xsl:template match="oai_cerif:Person">
    	<dc:type>Person</dc:type>
        <dc:Person> 
            <xsl:value-of select="@id"/>  
        </dc:Person>
        <dc:ORCID> 
            <xsl:value-of select="oai_cerif:ORCID" />  
        </dc:ORCID>
        <dc:ScopusAuthorID> 
            <xsl:value-of select="oai_cerif:ScopusAuthorID" /> 
        </dc:ScopusAuthorID>
        <dc:FamilyNames>
            <xsl:value-of select="oai_cerif:PersonName/oai_cerif:FamilyNames" /> 
        </dc:FamilyNames>
        <dc:FirstNames> 
            <xsl:value-of select="oai_cerif:PersonName/oai_cerif:FirstNames" /> 
        </dc:FirstNames>
        <dc:OrgUnit> 
            OrgUnits_<xsl:value-of select="oai_cerif:Affiliation/oai_cerif:OrgUnit/@id" /> 
        </dc:OrgUnit>
         <xsl:for-each select="oai_cerif:Affiliation/oai_cerif:OrgUnit">
        	 <dc:OrgUnitName> <xsl:value-of select="oai_cerif:Name" /></dc:OrgUnitName>
          </xsl:for-each>
        <xsl:for-each select="metadata/oai_cerif:Person/oai_cerif:Project">
            <dc:Project> 
                Projects_<xsl:value-of select="@id" /> | <xsl:value-of select="oai_cerif:Title" /> 
            </dc:Project>
        </xsl:for-each>
    </xsl:template>
    
    
    <xsl:template match="oai_cerif:OrgUnit">
    	<dc:type>OrgUnit</dc:type>
        <dc:OrgUnit>
            <xsl:value-of select="@id" /> 
        </dc:OrgUnit>
        <dc:Name> 
            <xsl:value-of select="oai_cerif:Name" />  
        </dc:Name>
        <dc:Acro> 
            <xsl:value-of select="oai_cerif:Acro" />  
        </dc:Acro>

    </xsl:template> 



    <xsl:template match="oai_cerif:Project">
    	<dc:type>Project</dc:type>
        <dc:Project> 
            <xsl:value-of select="@id" />  
        </dc:Project>
        <dc:Acronym> 
            <xsl:value-of select="oai_cerif:Acronym" />  
        </dc:Acronym>
        <dc:Title> 
            <xsl:value-of select="oai_cerif:Title" />  
        </dc:Title>
        <dc:StartDate>
        	 <xsl:value-of select="oai_cerif:StartDate" />
        </dc:StartDate>
        <dc:EndDate>
        	 <xsl:value-of select="oai_cerif:EndDate" />
        </dc:EndDate>
       <xsl:for-each select="oai_cerif:Team/oai_cerif:PrincipalInvestigator">
        	 <dc:Member>Persons_<xsl:value-of select="oai_cerif:Person/@id" />|<xsl:value-of select="oai_cerif:DisplayName"/></dc:Member>
       </xsl:for-each>
         <xsl:for-each select="oai_cerif:Team/oai_cerif:Member">
        	 <dc:Member>Persons_<xsl:value-of select="oai_cerif:Person/@id" />|<xsl:value-of select="oai_cerif:DisplayName"/></dc:Member>
       </xsl:for-each>
       <xsl:for-each select="oai_cerif:Funded/oai_cerif:By">
       <dc:FunderName> <xsl:value-of select="oai_cerif:DisplayName" /></dc:FunderName>
       <dc:Funder> Org_<xsl:value-of select="oai_cerif:OrgUnit/@id" /> </dc:Funder>
       </xsl:for-each>
        <xsl:for-each select="oai_cerif:Funded/oai_cerif:As/oai_cerif:Funding/oai_cerif:PartOf/oai_cerif:Funding">
       <dc:Funding> <xsl:value-of select="oai_cerif:Name" /></dc:Funding>
       </xsl:for-each>

      </xsl:template> 
   
   
   
    <xsl:template match="oai_cerif:Publication">
    	<dc:type>Publication</dc:type>
        <xsl:for-each select="oai_cerif:Title">
            <dc:title> 
                <xsl:value-of select="."/> 
            </dc:title>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:DOI">
            <dc:doi>
                <xsl:value-of select="."/> 
            </dc:doi>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:Handle">
            <dc:handle> 
                <xsl:value-of select="."/> 
            </dc:handle>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:PartOf">
            <dc:PartOf> 
                <xsl:value-of select="oai_cerif:DisplayName"/> 
            </dc:PartOf>
        </xsl:for-each>

         <xsl:for-each select="oai_cerif:Publishers">
         	 <xsl:for-each select="oai_cerif:Publisher">
            <dc:Publisher> 
                <xsl:value-of select="oai_cerif:DisplayName"/> 
            </dc:Publisher>
        	</xsl:for-each>
        </xsl:for-each>

          <xsl:for-each select="oai_cerif:OriginatesFrom">
          <xsl:for-each select="oai_cerif:Project">
            <dc:Project> 
                Projects_<xsl:value-of select="@id" /> | <xsl:value-of select="oai_cerif:Title" /> 
            </dc:Project>
        </xsl:for-each>
       </xsl:for-each>

        <xsl:for-each select="oai_cerif:URL">
            <dc:URL> 
                <xsl:value-of select="."/>  
            </dc:URL>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:URN">
            <dc:URN> 
                <xsl:value-of select="."/> 
            </dc:URN>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:Authors/oai_cerif:Author">
            <dc:Author> 
                Persons_<xsl:value-of select="oai_cerif:Person/@id" />|<xsl:value-of select="oai_cerif:DisplayName"/> 
            </dc:Author>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:Keyword">
            <dc:Keyword> 
                <xsl:value-of select="." /> 
            </dc:Keyword>
        </xsl:for-each>

        <xsl:for-each select="oai_cerif:Abstract">
            <dc:Abstract> 
                <xsl:value-of select="." /> 
            </dc:Abstract>
        </xsl:for-each>

    </xsl:template> 
</xsl:stylesheet>