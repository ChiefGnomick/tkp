package com.hakaton.tkp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import com.hakaton.tkp.dto.OrderDto;

@Service
public class DocsParserService {
    public byte[] generateDocs(OrderDto dto) throws IOException{
        try(XWPFDocument document = new XWPFDocument();){
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
    
            addBoldText(paragraph, "Заказ №");
            addNormalText(paragraph, Integer.toString(dto.getOrderNumber())+'\n');
    
            addBoldText(paragraph, "Перечень комплектующих: \n");
            for(int i = 1; i < dto.getComponents().size()+1; i++){
                addNormalText(paragraph, "  "+Integer.toString(i)+". "+dto.getComponents().get(i));
            }
    
            addBoldText(paragraph, "Итоговая цена: ");
            addNormalText(paragraph, dto.getFinalPrice().toString());
    
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();            
        } catch (Exception e) {
            return null;
        }
        
        
    }

    private void addBoldText(XWPFParagraph paragraph, String text) {
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
    }

    private void addNormalText(XWPFParagraph paragraph, String text) {
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(false);
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
    }
}
