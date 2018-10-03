/*
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *                   NOTICE
 *
 * This software was produced for the U. S. Government
 * under Basic Contract No. FA8702-17-C-0001, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (MAY 2013)
 *
 * (c)2016-2017 The MITRE Corporation. All Rights Reserved.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package org.mitre.tangerine.analytic;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bson.Document;
import org.mitre.tangerine.adapter.Analytic;
import org.mitre.tangerine.db.AETDatabase;
import org.mitre.tangerine.exception.AETException;
import org.mitre.tangerine.models.ResponseModel;
import org.mitre.tangerine.models.AssertionModel.ASSERT_TYPE;
import org.mitre.tangerine.models.AssertionModel;
import org.slf4j.Logger;

import com.google.gson.Gson;

public class IngestAdapter extends Analytic {
    private String name = "org.mitre.tangerine.analytic.GeneralAdapter", canonicalName = this.getClass().getName(),
                   description = "This adapter takes preconstructed assertions and inserts them into the knowledge base.";

    @Override
    public String getDatamap() throws IOException {
        throw new UnsupportedOperationException("This analytic does not have a data map");
    }

    @Override
    public void updateDB(ResponseModel data, AETDatabase db) throws AETException {
        Logger log = this.getLogger();
        List<AssertionModel> elements = data.getAssertions();
        String collection = data.getCollection();
        log.info("Opening connection to database");
        db.open();
        log.info("Accessing " + collection);
        db.access(collection);
        log.info("Adding assertions");
        for (AssertionModel assertion : elements) {
            db.update(Document.parse(new Gson().toJson(assertion)));
        }
        log.info("Indexing database");
        db.index();
        log.info("Closing connection to database");
        db.close();

    }

    @Override
    public ResponseModel adapt(String collection, InputStream data, Map<String, String> params)
    throws AETException, IOException {
        Logger log = this.getLogger();
        log.info("Parsing Assertions");
        ResponseModel rep = new ResponseModel();
        rep.setCollection(collection);
        Scanner s = new Scanner(data).useDelimiter("\\A");
        String input = s.hasNext() ? s.next() : "";
        data.close();

        //S: <uuid> P: a O: sumo#Text
        rep.addAssertion((new AssertionModel(collection, "a", "sumo#Text", ASSERT_TYPE.OBJECT)));
        //S: <uuid> P: label D: <the body of the file>
        rep.addAssertion((new AssertionModel(collection, "label", input, ASSERT_TYPE.DATA)));
        
        return rep;

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCanonicalName() {
        return this.canonicalName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
