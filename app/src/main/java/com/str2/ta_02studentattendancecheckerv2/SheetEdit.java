package com.str2.ta_02studentattendancecheckerv2;

/**
 * Created by The Administrator on 1/8/2015.
 */

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Using this demo, you can see how GData can read and write to individual cells
 * based on their position or send a batch of update commands in one HTTP
 * request.
 *
 * Usage: java CellDemo --username [user] --password [pass]
 */

public class SheetEdit {

    /** Our view of Google Spreadsheets as an authenticated Google user. */
    private SpreadsheetService service;

    /** The URL of the lists feed. */
    private URL listFeedUrl;

    /** A factory that generates the appropriate feed URLs. */
    private FeedURLFactory factory;

    /**
     * Constructs a cell demo from the specified spreadsheet service, which is
     * used to authenticate to and access Google Spreadsheets.
     *
     * @param service the connection to the Google Spradsheets service.
     */
    public SheetEdit(SpreadsheetService service) {
        this.service = service;
        this.factory = FeedURLFactory.getDefault();
    }

    public URL getListFeedUrl(){
        if(listFeedUrl != null) {
            return listFeedUrl;
        } else {
            return null;
        }
    }

    /**
     * Uses the user's credentials to get a list of spreadsheets. Then asks the
     * user which spreadsheet to load. If the selected spreadsheet has multiple
     * worksheets then the user will also be prompted to select what sheet to use.
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     *
     */
    public void loadWorksheet(SpreadsheetEntry spreadsheet, int worksheetIndex) throws IOException,
            ServiceException {
        // Get the worksheet to load
        List worksheets = spreadsheet.getWorksheets();
        WorksheetEntry worksheet = (WorksheetEntry) worksheets
                .get(worksheetIndex);
        listFeedUrl = worksheet.getListFeedUrl();
    }

    public SpreadsheetEntry loadSpreadsheet(int spreadsheetIndex) throws IOException,
            ServiceException {
        SpreadsheetFeed feed = service.getFeed(factory.getSpreadsheetsFeedUrl(),
                SpreadsheetFeed.class);
        return feed.getEntries().get(spreadsheetIndex);
    }

    public String showSheet(int index, List spreadsheets) throws IOException,
            ServiceException {
        BaseEntry entry = (BaseEntry) spreadsheets.get(index);
        return (entry.getTitle().getPlainText());
    }

    public List getSheetList() throws IOException,
            ServiceException {
        SpreadsheetFeed feed = service.getFeed(factory.getSpreadsheetsFeedUrl(),
                SpreadsheetFeed.class);
        List spreadsheets = feed.getEntries();
        return spreadsheets;
    }

    public void setRow(ListEntry row) throws IOException, ServiceException{
        service.insert(listFeedUrl, row);
    }
}
