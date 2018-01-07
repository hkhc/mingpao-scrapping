package io.hkhc.scrapping.mingpao;

import io.hkhc.utils.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class PDFTarget {

    private PDDocument document;
    private PDDocumentOutline outline;
    private PDOutlineItem rootOutline;


    public void start() {

        document = new PDDocument();

        outline =  new PDDocumentOutline();
        document.getDocumentCatalog().setDocumentOutline( outline );
        rootOutline = new PDOutlineItem();
        rootOutline.setTitle( "Ming Pao" );
        outline.addLast( rootOutline );

    }

    public void addPage(String pageName, InputStream imageStream) throws IOException {

        byte[] imageBytes = null;

        try {

            ByteArrayOutputStream tempImage = new ByteArrayOutputStream();
            FileUtils.writeStreamToStream(tempImage, imageStream);
            imageBytes = tempImage.toByteArray();

        }
        finally {
            imageStream.close();
        }

        BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(imageBytes));
        float width = bimg.getWidth();
        float height = bimg.getHeight();
        PDPage page = new PDPage(new PDRectangle(width, height));
        document.addPage(page);

        PDImageXObject img = PDImageXObject.createFromByteArray(document, imageBytes, pageName);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.drawImage(img, 0, 0);
        contentStream.close();


        PDPageDestination dest = new PDPageFitWidthDestination();

        dest.setPage( page );
        PDOutlineItem bookmark = new PDOutlineItem();
        bookmark.setDestination( dest );
        bookmark.setTitle( pageName );
        rootOutline.addLast( bookmark );


    }

    public void finish() {
        rootOutline.openNode();
        outline.openNode();
    }

    public void cleanup() throws IOException {

        document.close();

    }

    public void saveDocument(OutputStream os) throws IOException {

        document.save(os);

    }



}
