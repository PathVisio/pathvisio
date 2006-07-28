Hoi Martijn,
 
ik heb mijn files in een map gezet in de Visualizer repository (namelijk "Ensembl work").
Deze map bevat:
 
- Ensembl.java, de file waar enkele methoden in staan die door andere files gebruikt worden.
- ReferenceSearchAllGenes.java, deze file zoekt bij alle genen in de database de referenties en slaat deze op in references.txt.
- references.txt, de output file van ReferenceSearchAllGenes.java, met 3 kolommen: GenID, DatabaseNaam van de referentie, ReferentieID.
- ReferenceSearchByName.java, deze file zoekt bij een gegeven GenNaam alle referenties.
- EnsembVersoinCheck.java, deze file controleerd de versie van de locale database met die van de online ensembl database.
- UpdateEnsemblDatabase.java, deze file stuur de CommandLine aan met Ensembl.bat
- Ensembl.bat, hierin staan de processen die moeten plaatsvinden voor het maken van de locale database (bij het gedeelte van SQL werkt de aanroep niet, ik kreeg dit niet meer aan de gang).
- README for Ensembl database updater.doc, deze file bevat een korte hulp bij UpdateEnsemblDatabase.java
- enige .txt en .sql files die zorgen dat de database wordt aangemaakt. (hierbij zorgt Create_Ensembl_database.sql ervoor dat de overige files in de juiste volgorde worden aangeroepen)
 
Mochten er verder nog vragen zijn dan hoor ik het graag.
 
MvG,
Roeland
