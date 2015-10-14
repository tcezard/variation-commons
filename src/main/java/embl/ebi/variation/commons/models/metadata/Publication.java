package embl.ebi.variation.commons.models.metadata;

import java.util.*;

/**
 * Created by tom on 12/10/15.
 */
public class Publication {

    private int pmid;
    private String database;
    private String title;
    private String journal; // should journal be separate class?
    private String volume;
    private int startPage;
    private int endPage;
    private String doi;
    private String isbn;
    private Calendar publicationDate;
    private String firstAuthor;
    private List<String> authors = new ArrayList<>();
    private Set<Study> studies = new HashSet<>();


    public Publication(int pmid, String database, String title, String journal) {
        this.pmid = pmid;
        this.database = database;
        this.title = title;
        this.journal = journal;
    }

    public int getPmid() {
        return pmid;
    }

    public void setPmid(int pmid) {
        this.pmid = pmid;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Calendar getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Calendar publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getFirstAuthor() {
        return firstAuthor;
    }

    public void setFirstAuthor(String firstAuthor) {
        if (getAuthors().contains(firstAuthor)){
            this.firstAuthor = firstAuthor;
        }else{
            throw new IllegalArgumentException("Setting first author as an author that isn't in the list of authors.");
        }

    }

    public List<String> getAuthors() {
        return Collections.unmodifiableList(authors);
    }

    public void addAuthor(String author){
        authors.add(author);
    }

    public void setAuthors(List<String> authors) {
        this.authors.clear();
        for(String author: authors){
            addAuthor(author);
        }
    }

    public Set<Study> getStudies() {
        return Collections.unmodifiableSet(studies);
    }

    void removeStudy(Study study){
        studies.remove(study);
    }

    void addStudy(Study study){
        studies.add(study);
    }

    @Override
    public boolean equals(Object e) {
        if (e == this) {
            return true;
        }else if (!(e instanceof Publication)) {
            return false;
        }else{
            return (Objects.equals(((Publication) e).getPmid(), pmid));
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        return (31 * result + pmid);
    }
}
