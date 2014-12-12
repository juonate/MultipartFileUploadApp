package multipartfileuploadapp2;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 * This is a Swing application that demonstrates how to use the Jakarta
 * HttpClient multipart POST method for uploading files
 *
 * @author Sean C. Sullivan
 * @author Michael Becke
 *
 */
public class MultipartFileUploadApp2 {

    public static void main(String[] args) throws MalformedURLException, IOException {

        MultipartFileUploadFrame f = new MultipartFileUploadFrame();
        f.setTitle("Subir archivos");
        f.pack();
        f.addWindowListener(
                new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }

    public static class MultipartFileUploadFrame extends JFrame {

        private File targetFile;
        private JTextArea taTextResponse;
        private DefaultComboBoxModel cmbURLModel;
        String idParent = "60";
        String Parent = "PROGRAMA";

        public MultipartFileUploadFrame() throws MalformedURLException, IOException {

            String[] aURLs = {
                "http://localhost:8080/Adj/uploadFichero"
            };



            cmbURLModel = new DefaultComboBoxModel(aURLs);
            final JComboBox cmbURL = new JComboBox(cmbURLModel);
            cmbURL.setToolTipText("Entre una URL");
            cmbURL.setEditable(true);
            cmbURL.setSelectedIndex(0);

            JLabel lblTargetFile = new JLabel("Seleccione un archivo");

            final JTextField tfdTargetFile = new JTextField(30);
            tfdTargetFile.setEditable(false);

            final JCheckBox cbxExpectHeader = new JCheckBox(
                    "Use Expect header");

            cbxExpectHeader.setSelected(false);


            final JButton btnDoUpload = new JButton("Subir");
            btnDoUpload.setEnabled(false);

            final JButton btnSelectFile = new JButton("Seleccione");
            btnSelectFile.addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent evt) {

                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileHidingEnabled(false);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    chooser.setDialogTitle("Seleccione archivo...");

                    if (chooser.showOpenDialog(
                            MultipartFileUploadFrame.this)
                            == JFileChooser.APPROVE_OPTION) {
                        targetFile = chooser.getSelectedFile();
                        tfdTargetFile.setText(targetFile.toString());
                        btnDoUpload.setEnabled(true);

                    }
                }
            });

            taTextResponse = new JTextArea(10, 40);
            taTextResponse.setEditable(false);

            final JLabel lblURL = new JLabel("URL:");

            btnDoUpload.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {

                    try {
                        URL serverUrl = new URL("http://localhost:8080/Adj/uploadFichero");
                        HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");

                        BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                        httpRequestBodyWriter.write("Parent=Johnny+Jacobs&idParent=1234");
                        httpRequestBodyWriter.close();

                        Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
                        while (httpResponseScanner.hasNextLine()) {
                            System.out.println(httpResponseScanner.nextLine());
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(MultipartFileUploadApp2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MultipartFileUploadApp2.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    String targetURL = cmbURL.getSelectedItem().toString();
                    // add the URL to the combo model 
                    // if it's not already there


                    if (!targetURL
                            .equals(
                            cmbURLModel.getElementAt(
                            cmbURL.getSelectedIndex()))) {
                        cmbURLModel.addElement(targetURL);
                    }

                    PostMethod filePost = new PostMethod(targetURL);
                    filePost.addParameter("Parent", "Parent");


                    filePost.getParams().setBooleanParameter(
                            HttpMethodParams.USE_EXPECT_CONTINUE,
                            cbxExpectHeader.isSelected());

                    try {

                        appendMessage("Subiendo " + targetFile.getName()
                                + " a " + targetURL);

                        Part[] parts = {
                            new FilePart(targetFile.getName(), targetFile)
                        };

                        filePost.setRequestEntity(
                                new MultipartRequestEntity(parts,
                                filePost.getParams()));

                        HttpClient client = new HttpClient();
                        client.getHttpConnectionManager().
                                getParams().setConnectionTimeout(5000);
                        int status = client.executeMethod(filePost);

                        if (status == HttpStatus.SC_OK) {
                            appendMessage(
                                    "Subida completa, response="
                                    + filePost.getResponseBodyAsString());
                        } else {
                            appendMessage(
                                    "Subida fallida, response="
                                    + HttpStatus.getStatusText(status));
                        }
                    } catch (Exception ex) {
                        appendMessage("Error: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        filePost.releaseConnection();
                    }
                }
            });

            getContentPane().setLayout(new FlowLayout());
            /*
             GridBagConstraints c = new GridBagConstraints();
            
             c.anchor = GridBagConstraints.EAST;
             c.fill = GridBagConstraints.NONE;
             c.gridheight = 1;
             c.gridwidth = 1;
             c.gridx = 0;
             c.gridy = 0;
             c.insets = new Insets(10, 5, 5, 0);
             c.weightx = 1;
             c.weighty = 1;
             * */
            getContentPane().add(lblURL);
            /*
             c.anchor = GridBagConstraints.WEST;
             c.fill = GridBagConstraints.HORIZONTAL;
             c.gridwidth = 2;
             c.gridx = 1;
             c.insets = new Insets(5, 5, 5, 10);
             * 
             */
            getContentPane().add(cmbURL);
            /*
             c.anchor = GridBagConstraints.EAST;
             c.fill = GridBagConstraints.NONE;
             c.insets = new Insets(10, 5, 5, 0);
             c.gridwidth = 1;
             c.gridx = 0;
             c.gridy = 1;
             */
            getContentPane().add(lblTargetFile);
            /*
             c.anchor = GridBagConstraints.CENTER;
             c.fill = GridBagConstraints.HORIZONTAL;
             c.insets = new Insets(5, 5, 5, 5);
             c.gridwidth = 1;
             c.gridx = 1;
             */
            getContentPane().add(tfdTargetFile);
            /*
            
             c.anchor = GridBagConstraints.WEST;
             c.fill = GridBagConstraints.NONE;
             c.insets = new Insets(5, 5, 5, 10);
             c.gridwidth = 1;
             c.gridx = 2;
             */
            getContentPane().add(btnSelectFile);
            /*
            
             c.anchor = GridBagConstraints.CENTER;
             c.fill = GridBagConstraints.NONE;
             c.insets = new Insets(10, 10, 10, 10);
             c.gridwidth = 3;
             c.gridx = 0;
             c.gridy = 2;
             */
            getContentPane().add(cbxExpectHeader);
            /*
            
            
             c.anchor = GridBagConstraints.CENTER;
             c.fill = GridBagConstraints.NONE;
             c.insets = new Insets(10, 10, 10, 10);
             c.gridwidth = 3;
             c.gridx = 0;
             c.gridy = 3;
             */
            getContentPane().add(btnDoUpload);
            /*
             c.anchor = GridBagConstraints.CENTER;
             c.fill = GridBagConstraints.BOTH;
             c.insets = new Insets(10, 10, 10, 10);
             c.gridwidth = 3;
             c.gridheight = 3;
             c.weighty = 3;
             c.gridx = 0;
             c.gridy = 4;
             */
            getContentPane().add(new JScrollPane(taTextResponse));

        }

        private void appendMessage(String m) {
            taTextResponse.append(m + "\n");
        }
    }
}
