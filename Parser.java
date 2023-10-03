import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Parser {
    private static final Logger logger = LogManager.getLogger(Parser.class);

    public String[][] parseData() {
        try {
            Document document = Jsoup.connect("https://www.agh.edu.pl/studenci/aktualnosci").get();
            Elements articleElements = document.select(".col-12.offset-lg-3.col-lg-9.mb-lg-5.list-news a");
            
            String[][] data = new String[articleElements.size()][6];

            int row = 0;
            for (Element articleElement : articleElements) {
                Element titleElement = articleElement.selectFirst("h2");
                String title = titleElement != null ? titleElement.text() : "";
                data[row][0] = title;

                Element dateElement = articleElement.selectFirst("span");
                String date = dateElement != null ? dateElement.text() : "";
                data[row][1] = date;

                String href = articleElement.attr("href");
                String articleContent = getArticleContent(href);
                data[row][2] = articleContent;
                
                String[] links = getLinks(href);
                for(int i = 0; i < links.length; i++) {
                	if(links[i] != null) {
                	data[row][3+i] = links[i];
                	}
                }
                row++;
            }

            return data;
        } catch (IOException e) {
            logger.error("Failed to parse data: " + e.getMessage());
        }

        return new String[0][0];

    }

    private static String getArticleContent(String href) throws IOException {
        if (href.startsWith("/")) {
            String fullUrl = "https://www.agh.edu.pl" + href;
            Document articleDocument = Jsoup.connect(fullUrl).get();
            //Element mainContentElement = articleDocument.selectFirst("p");
            Elements mainContentElements = articleDocument.select("p");
            String content = "";
            for (Element mainContentElement : mainContentElements) {
            	content += mainContentElement.text() + " ";
            }
            Element restContentElement = articleDocument.selectFirst(".text-mt");
            return content + " " + restContentElement.text();
        } else {
            return href;
        }
    }

    private static String[] getLinks(String href) throws IOException {
    	if (href.startsWith("/")) {
            List<String> links = new ArrayList<>();
            String fullUrl = "https://www.agh.edu.pl" + href;
            Document articleDocument = Jsoup.connect(fullUrl).get();
            Element restContentElement = articleDocument.selectFirst(".text-mt");
            if (restContentElement != null) {
                Elements anchorElements = articleDocument.select(".text-mt a");
                if (!anchorElements.isEmpty()) {
                    for (Element anchorElement : anchorElements) {
                        String link = anchorElement.attr("href");
                        if(!link.startsWith("http")) {
                        	link = "https://www.agh.edu.pl" + link;
                        }
                        links.add(link);
                    }
                }
            }
            return links.toArray(new String[0]);
        } else {
        	String[] links = {""};
            return links;
        }
    }
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
