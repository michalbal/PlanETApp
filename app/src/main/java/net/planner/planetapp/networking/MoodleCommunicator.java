package net.planner.planetapp.networking;

import android.util.Xml;

import net.planner.planetapp.planner.PlannerTask;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.transform.TransformerFactoryConfigurationError;

public class MoodleCommunicator {
    public static final String MOODLE_URL_THIS_YEAR = "https://moodle2.cs.huji.ac.il/nu20";
    public static final String MOODLE_URL_NEXT_YEAR = "https://moodle2.cs.huji.ac.il/nu21";
    String moodleURL = MOODLE_URL_THIS_YEAR;

    private static String doHTTPRequest(String url) throws ClientProtocolException, IOException, JSONException {
        String responseBody;
        String token = "";

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpPost = new HttpGet(url);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        responseBody = httpClient.execute(httpPost, responseHandler);

        JSONObject jObject = new JSONObject(responseBody);
        token = jObject.getString("token");

        return token;
    }

    public String connectToCSEMoodle(String username, String password, Boolean isThisYear) throws ClientProtocolException, IOException, JSONException {
        if (isThisYear) {
            moodleURL = MOODLE_URL_THIS_YEAR;
        } else {
            moodleURL = MOODLE_URL_NEXT_YEAR;
        }
        String url =
                moodleURL + "/login/token.php?username=" + username + "&password=" + password +
                "&service=moodle_mobile_app";
        return doHTTPRequest(url);
    }

    public HashMap parseFromMoodle(String token, Boolean onlyCourseNames) {
        String serverurl = moodleURL + "/webservice/rest/server.php" + "?wstoken=" + token +
                           "&wsfunction=mod_assign_get_assignments";
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(serverurl).openConnection();
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Language", "en-US");
            con.setDoOutput(true);
            con.setUseCaches(false);

            //Get Response
            InputStream is = con.getInputStream();
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();

            return parseXML(parser, onlyCourseNames);

        } catch (TransformerFactoryConfigurationError | IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }


    private HashMap parseXML(XmlPullParser parser, Boolean onlyCourseNames)
            throws XmlPullParserException, IOException {
        String ns = null;
        HashMap<String, LinkedList<PlannerTask>> parsedAssignments = new HashMap<>();
        HashMap<String, String> parsedCourses = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, ns, "RESPONSE");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("SINGLE") && parser.getDepth() == 5) {
                if (onlyCourseNames) {
                    parsedCourses.putAll(readCourseNames(parser));
                } else {
                    parsedAssignments.putAll(readCourse(parser));
                }
            }
        }
        return onlyCourseNames ? parsedCourses : parsedAssignments;
    }

    private HashMap<String, String> readCourseNames(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        HashMap<String, String> courseNames = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, null, "SINGLE");
        String courseId = "";
        String courseFullname = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String attrName = parser.getAttributeValue("", "name");
            if (attrName != null && attrName.equals("id")) {
                courseId = readText(parser);
            } else if (attrName != null && attrName.equals("fullname")) {
                courseFullname = readText(parser);
                courseNames.put(courseId, courseFullname);
            } else {
                skip(parser);
            }
        }
        return courseNames;
    }

    private HashMap<String, LinkedList<PlannerTask>> readCourse(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        HashMap<String, LinkedList<PlannerTask>> assignments = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, null, "SINGLE");
        String courseId = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String attrName = parser.getAttributeValue("", "name");
            if (attrName != null && attrName.equals("id")) {
                courseId = readText(parser);
            } else if (attrName != null && attrName.equals("assignments")) {
                assignments.put(courseId, readAssignments(parser, courseId));
            } else {
                skip(parser);
            }
        }
        return assignments;
    }

    private LinkedList<PlannerTask> readAssignments(XmlPullParser parser, String courseId)
            throws IOException, XmlPullParserException {
        LinkedList<PlannerTask> assignments = new LinkedList<>();
        parser.next();
        if (!parser.getName().equals("MULTIPLE")) {
            return assignments;
        }
        parser.require(XmlPullParser.START_TAG, null, "MULTIPLE");
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            parser.require(XmlPullParser.START_TAG, null, "SINGLE");
            String name = "";
            String id = "";
            long duedate = 0L;
            while (parser.nextTag() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG || !parser.getName().equals(
                        "KEY")) {
                    continue;
                }
                String assignmentAttrName = parser.getAttributeValue("", "name");
                if (assignmentAttrName != null && assignmentAttrName.equals("name")) {
                    name = courseId + ": " + readText(parser);
                } else if (assignmentAttrName != null && assignmentAttrName.equals("duedate")) {
                    duedate = Long.parseLong(readText(parser));
                } else if (assignmentAttrName != null && assignmentAttrName.equals("id")) {
                    id = readText(parser);
                } else {
                    skip(parser);
                }
            }
            // We convert from seconds to milliseconds before creating the task.
            PlannerTask task = new PlannerTask(name, duedate * 1000L, 5 * 60);
            task.setCourseId(courseId);
            task.setMoodleId(id);
            assignments.add(task);
        }
        parser.require(XmlPullParser.END_TAG, null, "MULTIPLE");
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, "KEY");
        return assignments;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "VALUE");
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, "VALUE");
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, "KEY");
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
