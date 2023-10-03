import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Program {
    private static final Logger logger = LogManager.getLogger(Program.class);

    public static void main(String[] args) {
        Parser parser = new Parser();
        String[][] data = parser.parseData();

        // Tworzenie JTable
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Zmiana trybu zamykania na DISPOSE_ON_CLOSE
        String[] columnNames = {"Tytuł artykułu", "Data artykułu", "Treść artykułu", "Link 1", "Link 2"};
        DefaultTableModel model = new DefaultTableModel(data, columnNames);

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Wyłączenie automatycznego dostosowywania szerokości

        
        
        // Dostosowanie szerokości kolumn
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        for (int column = 0; column < columnCount; column++) {
            int preferredWidth = columnModel.getColumn(column).getPreferredWidth();
            int maxWidth = columnModel.getColumn(column).getMaxWidth();
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
                // Ustal maksymalną szerokość kolumny, jeśli jest dostępna
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }
            columnModel.getColumn(column).setPreferredWidth(preferredWidth);
        }
        
     // Dodawanie pola tekstowego do filtrowania
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
            
            // Metoda filtrujaca z uwzględnieniem nie rozróżniania wielkości liter
            private void filterTable() {
                String searchText = searchField.getText().toLowerCase();
                TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(model);
                table.setRowSorter(rowSorter);

                try {
                    RowFilter<DefaultTableModel, Object> rowFilter = new RowFilter<DefaultTableModel, Object>() {
                        @Override
                        public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                            int valueCount = entry.getValueCount();
                            for (int i = 0; i < valueCount; i++) {
                                String value = String.valueOf(entry.getValue(i)).toLowerCase();
                                if (value.contains(searchText)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    };
                    rowSorter.setRowFilter(rowFilter);
                } catch (PatternSyntaxException ex) {
                    ex.printStackTrace();
                }
            }

        });

        //Dodawanie obsługi zdarzeń kliknięcia w tabeli
        table.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                // Sprawdzanie, czy kliknięto na komórkę zawierającą link
                if (column == 3 || column == 4) {
                    String link = (String) table.getValueAt(row, column);
                    if (!link.isEmpty()) {
                        openLinkInBrowser(link);
                    }
                }
            }
        });
        
        
        // Nasłuchiwanie zdarzenia zamknięcia ramki
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                logger.info("Zamknięcie programu");
                org.apache.logging.log4j.LogManager.shutdown();

            }
        });
        
        
        JScrollPane scrollPane = new JScrollPane(table);
        table.setRowHeight(50);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.add(searchField, BorderLayout.NORTH);
        frame.pack(); // Dopasowanie rozmiaru kolumn do zawartości
        frame.setSize(800, 400);
        frame.setVisible(true);      
    }
    
    
    private static void openLinkInBrowser(String link) {
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (IOException | URISyntaxException ex) {
            logger.error("Failed to open link: " + ex.getMessage());
        }
    }
}


