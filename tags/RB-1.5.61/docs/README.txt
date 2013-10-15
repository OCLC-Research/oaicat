To upgrade OAICat with the latest code changes, copy the latest
oaicat.jar file to webapps/oaicat/WEB-INF/lib/.

Before customizing OAICat, first install oaicat.war in a J2EE Servlet
Engine and verify that the default configuration works. If so, proceed
with any necessary code and configuration changes as described below.

Before building this probject with Ant, create a 'build.properties'
file in the project directory with the following entries:

catalina.home=/path/to/jakarta-tomcat

To create a new distribution set, issue the command:

ant dist

To customize OAICat, answer these questions:

Q1: What Java package should I use to hold my custom classes?
    a) For example, if you work for Acme Inc., create a directory
       hierarchy somewhere named:

       com/acme/oai

Q2: What database engine will I use?
    a) For example, if using the Foo database, copy
       oaicatjar/src/ORG/oclc/oai/server/catalog/DummyOAICatalog.java
       to com/acme/oai/server/catalog/FooOAICatalog.java and modify the
       code so the class name matches the new filename.
    b) Change the code in this class to use the Foo database Java API.
       In general, all this class needs to know about the records
       is that they are black-box Java Objects. To make life easier
       downstream, however, it may be worthwhile to convert the records
       to a more convenient processing form immediately after reading.
       For example, if the records are stored as XML Strings, load
       them into DOM objects as soon as they are read. Beyond that,
       though, leave it to the Crosswalk and RecordFactory
       implementations to understand the true semantics of the records.
       Doing this may mean you can't reuse this class for cases where
       the database returns non-XML byte arrays, but then again, what
       are the chances of that?
    c) Make a corresponding package/class name change to the
       AbstractCatalog.oaiCatalogClassName entry in the
       webapps/oaicat/WEB-INF/oaicat.properties file to have OAICat use
       your custom class.

Q3: What are the semantics of these record objects?
    a) If FooOAICatalog returns records as byte arrays, examples can be
       anything such as MARC Communications Format. If FooOAICatalog
       returns Strings, examples might include MARC BER, or any kind of
       XML String. If FooOAICatalog returns DOM Documents, examples can
       be any XML-based metadata format. Let's assume FooOAICatalog
       returns records as DOM Documents containing MARCXML content.
    b) Copy oaicatjar/src/ORG/oclc/oai/server/catalog/XMLRecordFactory.java
       to com/acme/oai/server/catalog/MARCXMLDOMRecordFactory.java
       and modify the code so the class name matches the new filename.
    c) Change the methods to cast each Object nativeItem parameter to a
       org.w3c.dom.Document and use it to extract the relevant data for
       each method.
    d) Make a corresponding package/class name change to the
       AbstractCatalog.recordFactoryClassName entry in the
       webapps/oaicat/WEB-INF/oaicat.properties file to have OAICat use
       your custom class.

Q4: What OAI metadatdaFormats will be supported?
    a) Examples include oai_dc, marcxml, or oai_etdms.
    b) For oai_dc, copy oaicatjar/src/ORG/oclc/oai/crosswalk/XML2oai_dc.java
       to com/acme/oai/server/catalog/MARCXMLDOM2oai_dc.java and modify
       the code so the class name matches the new filename.
    c) Change the constructor to use the appropriate schemaLocation for
       this metadataFormat.
    d) Change the methods to cast each Object nativeItem parameter to a
       org.w3c.dom.Document and use it to service the method accordingly.
       In this case, you could use the Library of Congress MARCXML to DC
       XSL stylesheet (see http://www.loc.gov/standards/marcxml/) to
       perform the crosswalk to Dublin Core.
    e) Repeat steps b, c, and d for each metadatdaFormat to be supported.
    f) Make a corresponding package/class name change to the
       Crosswalks.* entries in the webapps/oaicat/WEB-INF/oaicat.properties
       file to have OAICat use your custom classes.

Finally, change other properties in oaicat.properties according to your
preferences.

That's essentially what it takes to customize OAICat. Contact Jeff Young
at jyoung@oclc.org with questions and comments.
