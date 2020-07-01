/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import commandline.Command;
import commandline.CommandLine;
import commandline.executables.youtubedl.YoutubeDL;
import commandline.executables.youtubedl.YoutubeDL.Options;
import commandline.executables.youtubedl.YoutubeDLBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ManagedBean
@RequestScoped
public class Handler {

    private String url;
    private String format;
    private String downloadURL;
    private String downloadLink;
    private Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    HttpServletRequest resquest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
    ServletContext servletContext = (ServletContext) FacesContext
            .getCurrentInstance().getExternalContext().getContext();

//    pogressbar Navigation
    public String gotopogresspage() {
        sessionMap.put("url", url);
        sessionMap.put("format", format);
        return "pogresspage.xhtml?faces-redirect=true";
    }

//    Start the download to server when pogressbar show
    public String doStart(String durl, String format) {
        System.out.println("Downlod started againg");
        YoutubeDL.setExecutable("resources/youtube-dl.exe");
        YoutubeDLBuilder builder = new YoutubeDLBuilder();
        builder.setTermination(Command.DEFAULT_TERMINATION_LOGGER);
        builder.setInitiation(Command.DEFAULT_INITIATION_LOGGER);
        builder.setConsole(Command.DEFAULT_CONSOLE_LOGGER);

        if (format.equals("mp4")) {
            builder.option(Options.FORMAT, "best");
            builder.option(Options.OUTPUT, getLocation("mp4"));
        }
        if (format.equals("mp3")) {
            builder.property(YoutubeDL.Properties.EXTRACT_AUDIO);
            builder.option(Options.AUDIO_FORMAT, "mp3");
            builder.option(Options.OUTPUT, getLocation("mp3"));
        }
        builder.url(durl);
        Command youtubeDL = builder.build();
        new CommandLine().queueCommand(youtubeDL);
        System.out.println("Download Complete");
        System.out.println(resquest.getHeader("host"));
        return "getlink.xhtml?faces-redirect=true";
    }

    private String getLocation(String ext) {
        String name = System.currentTimeMillis() + "";
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        downloadLink = "http://" + request.getHeader("host") + "/" + name + "." + ext;
        sessionMap.put("downloadLink", downloadLink);
        sessionMap.put("filename", name + "." + ext);
        System.out.println(downloadLink);
        return "..\\webapps/ROOT/" + name + "." + ext;
    }

    public void DownloadWebPage() throws FileNotFoundException, IOException {
        PrintWriter out = null;
        String fileName = sessionMap.get("filename").toString();
        // reads input file from an absolute path
        String filePath = "..\\webapps/ROOT/" + fileName;
        File downloadFile = new File(filePath);
        FileInputStream inStream = new FileInputStream(downloadFile);

        // if you want to use a relative path to context root:
        String relativePath = servletContext.getRealPath("");
        System.out.println("relativePath = " + relativePath);

        // obtains ServletContext
//        ServletContext context = servletContext;

        // gets MIME type of the file
        String mimeType = servletContext.getMimeType(filePath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // modifies response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // obtains response's output stream
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }

//    Getter and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public Map<String, Object> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
