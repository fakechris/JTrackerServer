<%-- 
    Document   : torrentlist
    Created on : 14-Nov-2008, 21:00:56
    Author     : bo
--%>

<%@page
contentType="application/json"
pageEncoding="UTF-8"
import="javax.persistence.EntityManager"
import="javax.persistence.EntityManagerFactory"
import="javax.persistence.NoResultException"
import="javax.persistence.Persistence"
import="javax.persistence.Query"
import="java.util.Vector"
import="java.util.Iterator"
import="java.util.Map"
import="java.util.Date"
import="com.tracker.entity.Torrent"
import="com.tracker.entity.Peer"
%>
<%
EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
EntityManager em = emf.createEntityManager();
Query q;
StringBuilder query = new StringBuilder();
StringBuilder jsonResponse = new StringBuilder();
boolean searchDescriptions = false;
boolean includeDead = false;
Map<String,String[]> params = request.getParameterMap();
Vector<Torrent> result;
Iterator itr;

// do we search for anything?
if(params.containsKey((String)"searchField")) {
    // search for the search string given
    // (there is only one object of this anyway)
    String searchString = params.get((String)"searchField")[0];

    // do we search descriptions?
    if(params.containsKey((String)"searchDescriptions")) {
        String value = params.get((String)"searchDescriptions")[0];
        if(value.equalsIgnoreCase("checked")) {
            searchDescriptions = true;
        }
    }

    // do we search dead torrents?
    if(params.containsKey((String)"includeDead")) {
        String value = params.get((String)"includeDead")[0];
        if(value.equalsIgnoreCase("checked")) {
            includeDead = true;
        }
    }

    // create the query
    query.append("SELECT t FROM Torrent t WHERE t.name LIKE ");
    // horrible SQL-injection waiting to happen? :D
    query.append("%" + searchString + "%");
    if(searchDescriptions) {
        query.append(" OR t.description LIKE ");
        query.append("%" + searchString + "%");
    }
    if(includeDead) {
        query.append(" AND t.numSeeders > 0 OR t.numLeechers > 0");
    }


}
// no search string given
else {
    // TODO: 50 per page?
    query.append("SELECT t FROM Torrent t");
}

q = em.createQuery(query.toString());
result = (Vector<Torrent>) q.getResultList();

{
    // add some torrents to test
    Date d = new Date();
    d.setTime(1226703180000L);
    Torrent t = new Torrent();
    t.setName("test-torrent1");
    t.setDescription("dafdasfdasfas");
    t.setAdded(d);
    result.add(t);

    d = new Date();
    t = new Torrent();
    d.setTime(1226603180000L);
    t.setName("test-torrent2");
    t.setDescription("fdsfgsfdsfs");
    t.setAdded(d);
    result.add(t);
}

itr = result.iterator();

{
    // TODO: replace with the JSON*-classes
    StringBuilder name = new StringBuilder();
    StringBuilder date = new StringBuilder();
    jsonResponse.append("{\n");
    name.append("\t\"name\": [\n");
    date.append("\t\"date\": [\n");
    char delim = ' ';
    while(itr.hasNext()) {
        Torrent t = (Torrent) itr.next();
        name.append(delim + "\n\t\t\"" + t.getName() + "\"");
        date.append(delim + "\n\t\t\"" + t.getAdded().toString() + "\"");
        delim = ',';
    }
    name.append("\n\t],\n");
    date.append("\n\t]\n");
    jsonResponse.append(name);
    jsonResponse.append(date);
    jsonResponse.append("}");
}

out.print(jsonResponse.toString());
%>
