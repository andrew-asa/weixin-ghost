package com.asa.weixin.spider.controller;

import com.asa.base.enent.Event;
import com.asa.base.enent.EventDispatcher;
import com.asa.base.enent.Listener;
import com.asa.log.LoggerFactory;
import com.asa.utils.ListUtils;
import com.asa.utils.StringUtils;
import com.asa.weixin.spider.Spider;
import com.asa.weixin.spider.model.WeixinArticle;
import com.asa.weixin.spider.service.pdf.HtmlToPdfService;
import com.asa.weixin.spider.ui.component.Toast;
import com.asa.weixin.spider.view.ArticleListPaneView;
import com.asa.weixin.spider.view.WeixinArticleReadView;
import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author andrew_asa
 * @date 2021/3/26.
 * 阅读公众号文章
 */
@FXMLController
public class WeixinArticleReadController implements Initializable {

    @FXML
    private WebView webContainer;

    @FXML
    private Button back;

    @FXML
    private Button forward;

    @FXML
    private Button pageHome;

    @FXML
    private Button pdfConverter;

    private WebEngine webEngine;

    @Autowired
    private HtmlToPdfService htmlToPdfService;

    private WeixinArticle weixinArticle;

    public enum WeixinArticleReadEvent implements Event<WeixinArticle> {

        /**
         * 请求加载url
         */
        REQUIRE_LOAD,
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        LoggerFactory.getLogger().debug("WeixinArticleReadController initialize");
        initListener();
        initWebEngine();
        initButtonAction();
    }

    private void initButtonAction() {
        initBackButton();
        initPageHomeButton();
        initPdfConverterButton();
        initForwardButton();
    }

    private void initBackButton() {
        back.setOnMouseClicked(e-> back());
        back.setTooltip(new Tooltip("后退"));
    }

    private void initForwardButton() {
        forward.setOnMouseClicked(e-> forward());
        forward.setTooltip(new Tooltip("前进"));
    }


    private void initPageHomeButton() {
        pageHome.setOnMouseClicked(e-> pageHome());
        pageHome.setTooltip(new Tooltip("返回文章列表"));
    }
    private void initPdfConverterButton() {
        pdfConverter.setOnMouseClicked(e-> pdfConverter());
        pdfConverter.setTooltip(new Tooltip("导出为pdf"));
    }

    public void pdfConverter() {
        LoggerFactory.getLogger().debug("pdfConverter");
        try {
            Printer printer = findPdfWriterPrint();
            if (printer == null) {
                Toast.makeText("请先安装pdfwriterformac").showOnRight(pdfConverter);
            } else {
                PrinterJob job = PrinterJob.createPrinterJob(printer);
                //job.showPageSetupDialog(Spider.getStage());
                job.showPrintDialog(Spider.getStage());
                job.getJobSettings().setJobName(weixinArticle.getTitle());
                //job.;
                if (job != null) {
                    webEngine.print(job);
                    job.endJob();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Printer findPdfWriterPrint() {
        Printer[] printers =  Printer.getAllPrinters().toArray(new Printer[0]);
        if (printers != null) {
            for (Printer printer : printers) {
                if (StringUtils.equalsIgnoreCase(printer.getName(),"pdfwriter")) {
                    return printer;
                }
            }
        }
        return null;
    }

    public void forward() {
        final WebHistory history=webEngine.getHistory();
        ObservableList<WebHistory.Entry> entryList=history.getEntries();
        int currentIndex=history.getCurrentIndex();
        if (currentIndex + 1 < ListUtils.length(entryList)) {
            history.go(1);
            LoggerFactory.getLogger().debug("forward {}",entryList.get(currentIndex<entryList.size()-1?currentIndex+1:currentIndex).getUrl());
        }
    }

    public void back() {
        final WebHistory history=webEngine.getHistory();
        ObservableList<WebHistory.Entry> entryList=history.getEntries();
        int currentIndex=history.getCurrentIndex();
        if (currentIndex > 0) {
            history.go(-1);
            //LoggerFactory.getLogger().debug("back {}",entryList.get(currentIndex>0?currentIndex-1:currentIndex).getUrl());
        } else {
            pageHome();
        }
    }

    public void pageHome() {
        EventDispatcher.fire(HomePageSubPanelEvent.REQUIRE_INSTALL, ArticleListPaneView.NAME);
    }

    private void initWebEngine() {
        webEngine = webContainer.getEngine();
        //webEngine.getLoadWorker().stateProperty()
        //        .addListener(new ChangeListener<State>() {
        //            @Override
        //            public void changed(ObservableValue ov, State oldState, State newState) {
        //
        //                if (newState == Worker.State.SUCCEEDED) {
        //                    stage.setTitle(webEngine.getLocation());
        //                }
        //
        //            }
        //        });
    }

    private void initListener() {

        EventDispatcher.listen(WeixinArticleReadEvent.REQUIRE_LOAD, new Listener<WeixinArticle>() {

            @Override
            public void on(Event event, WeixinArticle param) {
                EventDispatcher.fire(HomePageSubPanelEvent.REQUIRE_INSTALL, WeixinArticleReadView.NAME);
                weixinArticle = param;
                webEngine.load(param.getLink());
            }
        });
    }
}
