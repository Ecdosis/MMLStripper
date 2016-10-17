/*
 * This file is part of MMLStripper.
 *
 *  MMLStripper is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  MMLStripper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MMLStripper.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2016
 */
package mmlstripper;
import mmlstripper.stil.Range;
import mmlstripper.stil.STILDocument;
import org.json.simple.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Stack;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
/**
 * Read a single mml file or a folder of them and strip out the tags
 * @author desmond
 */
public class MMLStripper {
    JSONObject dialect;
    String style;
    File target;
    File dest;
    File textDir;
    File stilDir;
    GlobalExchange ge;
    boolean writeAnonSections;
    Stack<String> dirs;
    STILDocument corcodeDefault;
    STILDocument corcodePages;
    StringBuilder cortex;
    ArrayList<ParaFormat> paraFormats;
    ArrayList<LineFormat> lineFormats;
    ArrayList<MMLObject> charFormats;
    ArrayList<Setext> setexts;
    MMLStripper()
    {
        paraFormats = new ArrayList<ParaFormat>();
        lineFormats = new ArrayList<LineFormat>();
        charFormats = new ArrayList<MMLObject>();
        setexts = new ArrayList<Setext>();
    }
    static String readFile( File f )
    {
        try
        {
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[(int)f.length()];
            fis.read(data);
            fis.close();
            return new String(data,"UTF-8");
        }
        catch ( Exception e )
        {
            return "";
        }
    }
    /**
     * Read the paragraph formats from the dialect file
     * @param pfmts the para formats as an ArrayList
     */
    void readParaFormats( JSONArray pfmts )
    {
        for ( int i=0;i<pfmts.size();i++ )
        {
            JSONObject jObj = (JSONObject)pfmts.get(i);
            ParaFormat pf = new ParaFormat(
                (String)jObj.get("leftTag"),
                (String)jObj.get("rightTag"),
                (String)jObj.get("prop"));
            paraFormats.add(pf);
        }
    }
    /**
     * Read the paragraph formats from the dialect file
     * @param lfmts the line formats as an ArrayList
     */
    void readLineFormats( JSONArray lfmts )
    {
        for ( int i=0;i<lfmts.size();i++ )
        {
            JSONObject jObj = (JSONObject)lfmts.get(i);
            LineFormat lf = new LineFormat(
                (String)jObj.get("leftTag"),
                (String)jObj.get("rightTag"),
                (String)jObj.get("prop"));
            lineFormats.add(lf);
        }
        sortList(lineFormats);
    }
    void readCharFormats( JSONArray cfmts )
    {
        for ( int i=0;i<cfmts.size();i++ )
        {
            JSONObject jObj = (JSONObject)cfmts.get(i);
            String tag = (String)jObj.get("tag");
            String leftTag = (String)jObj.get("leftTag");
            String rightTag = (String)jObj.get("rightTag");
            if ( leftTag == null )
                leftTag = tag;
            if ( rightTag == null )
                rightTag = tag;
            CharFormat cf = new CharFormat(
                leftTag,
                rightTag,
                (String)jObj.get("prop"));
            charFormats.add(cf);
        }
        sortList(charFormats);
    }
    /**
     * Read predefined globals into the globals object
     * @param globals an array of swap-strings (seq, rep)
     */
    void readGlobals( JSONArray globals )
    {
        for ( int i=0;i<globals.size();i++ )
        {
            JSONObject jObj = (JSONObject)globals.get(i);
            if ( ge == null )
            {
                ge = new GlobalExchange(
                    (String)jObj.get("seq"),
                    (String)jObj.get("rep"));
            }
            else
                ge.append((String)jObj.get("seq"),(String)jObj.get("rep"));
        }
    }
    /**
     * Interpret smart uotes as globals
     * @param on if true add smartquotes as globals
     */
    void readSmartQuotes( Boolean on )
    {
        if ( on.booleanValue() == true )
        {
            if ( ge == null )
            {
                ge = new GlobalExchange(" \""," “" );
            }
            else
                ge.append(" \""," “" );
            // these are for quotes at line-start
            ge.append("\000'","‘" );
            ge.append("\000\"","“" );
            ge.append(" '"," ‘" );
            ge.append("\"","”");
            ge.append("'","’");
        }
    }
    void readHeadings( JSONArray headings )
    {
        for ( int i=0;i<headings.size();i++ )
        {
            JSONObject jObj = (JSONObject)headings.get(i);
            Setext st = new Setext( 
                (String)jObj.get("tag"),
                (String)jObj.get("prop")
            );
            setexts.add( st );
        }
    }
    /**
     * Read in the dialect file and create the properties map
     * @param path the path to the file
     * @return true if it worked
     */
    boolean readDialectFile( String path )
    {
        File f = new File(path);
        if ( f.exists() )
        {
            try
            {
                String json = readFile(f);
                dialect = (JSONObject) JSONValue.parse(json);
                Set<String> keys = dialect.keySet();
                Iterator<String> iter = keys.iterator();
                while ( iter.hasNext() )
                {
                    String key = iter.next();
                    DialectKeys dk = DialectKeys.valueOf(key);
                    switch( dk )
                    {
                        case smartquotes:
                            readSmartQuotes((Boolean)dialect.get("smartquotes"));
                            break;
                        case headings:
                            readHeadings((JSONArray)dialect.get("headings"));
                            break;
                        case globals:
                            readGlobals((JSONArray)dialect.get("globals"));
                            break;
                        case charformats:
                            readCharFormats((JSONArray)dialect.get("charformats"));
                            break;
                        case lineformats:
                            readLineFormats((JSONArray)dialect.get("lineformats"));
                            break;
                        case paraformats:
                            readParaFormats((JSONArray)dialect.get("paraformats"));
                            break;
                        default:
                            System.out.println("Ignoring key "+key);
                            break;
                    }
                }
                
                return true;
            }
            catch ( Exception e )
            {
                System.out.println(e.getMessage());
                return false;
            }
        }
        else
            return false;
    }
    void sortList(ArrayList a) 
    {
        int increment = a.size() / 2;
        while (increment > 0) {
            for (int i = increment; i < a.size(); i++) {
                int j = i;
                Comparable temp = (Comparable)a.get(i);
                // sort by dereasing order of length
                while (j >= increment 
                    && ((Comparable)a.get(j-increment)).compareTo(temp)<0) {
                    a.set(i, a.get(j-increment));
                    j = j - increment;
                }
                a.set(j, temp);
            }
            if (increment == 2) {
                increment = 1;
            } else {
                increment *= (5.0 / 11);
            }
        }
    }
    /**
     * Is the string wholly composed of whitespace?
     * @param text the text to test
     * @return true if it is
     */
    boolean isWhitespace( String text )
    {
        for ( int i=0;i<text.length();i++ )
        {
            if ( !Character.isWhitespace(text.charAt(i)) )
                return false;
        }
        return text.length()>0;
    }
    /**
     * Process a line character by character
     * @param line the line to process
     * @param offset the offset at line start
     * @return the processed line text
     */
    String processCharacters( String line, int offset ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        Range r = null;
        MMLRet ret = new MMLRet(false,null);
        Stack<Range> stack = new Stack<Range>();
        // do globals first to avoid interference with charformats
        if ( ge != null )
        {
            ge.reset();
            if ( ge.startsWith('\000') )
                line = '\000'+line;
            int i=0;    // allow for tokens to be pushed back on input
            while ( i < line.length() || ret.pushed != 0 )
            {
                char token;
                if ( ret.pushed != 0 )
                {
                    token = ret.pushed;
                    ret.pushed = 0;
                }
                else 
                    token = line.charAt(i++);
                ret = ge.add(token,ret);
                if ( ret.result != null && ret.result.length()>0 )
                    sb.append(ret.result);
                else if ( !ret.active && ret.result == null )
                    sb.append( token );
            }
            // clean up at line-end
            String res = ge.flush();
            if ( res != null )
                sb.append( res );
            line = sb.toString();
            sb.setLength(0);
        }
        // now do the charformats one by one
        for ( int i=0;i<line.length();i++ )
        {
            char token = line.charAt(i);
            boolean noTakers = true;
            // apply charformats
            for ( int j=0;j<charFormats.size();j++ )
            {
                MMLObject obj = charFormats.get(j);
                ret = obj.add(token,ret);
                if ( ret.result != null || ret.active )
                    noTakers = false;
                if ( ret.result != null )
                {
                    if ( ret.isProperty() )
                    {
                        r = new Range( ret.result.substring(1,ret.result.length()-1) );
                        stack.push(r);
                        if ( r.isPage() )
                            corcodePages.add(r);
                        else
                            corcodeDefault.add(r);
                        r.setOffset( offset+sb.length());
                    }
                    else if ( ret.result.length()==0 )
                    {
                        r.setLen(offset+sb.length()-r.getOffset());
                        r = stack.pop();
                    }
                    else
                    {
                        // echoed text
                        sb.append(ret.result);
                    }
                }
            }
            if ( noTakers )
                sb.append(token);
        }
        // in case char format not terminated properly
        if ( ret.active && r != null )
            r.setLen(sb.length()-r.getOffset());
        // reset all charformats at line-end to their initial states
        for ( int i=0;i<charFormats.size();i++ )
            charFormats.get(i).reset();
        return sb.toString();
    }
    /**
     * Apply line formats
     * @param line the line to format
     * @param offset the offset at line-start
     * @param llf the format of the previous line
     * @return a fully processed line
     * @throws Exception 
     */
    String processLine( String line, int offset, LastLineFormat llf ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        Range r = null;
        for ( int i=0;i<lineFormats.size();i++ )
        {
            LineFormat lf = lineFormats.get(i);
            if ( line.startsWith(lf.startTag) && line.endsWith(lf.endTag) )
            {
                line = line.substring(lf.startTag.length(),
                    line.length()-lf.endTag.length());
                if ( llf.range == null || !lf.prop.equals(llf.range.getName()) )
                {
                    r = new Range( lf.prop );
                    r.setOffset(offset);
                    llf.range = r;
                    if ( r.isPage() )
                        corcodePages.add(r);
                    else
                        corcodeDefault.add(r);
                }
                else
                    r = llf.range;
                sb.append( processCharacters(line,offset) );
                break;
            }
        }
        // no line format
        if ( r == null )
        {
            llf.range = null;
            sb.append( processCharacters(line,offset) );
        }
        line = sb.toString().trim();
        if ( llf.range != null )
            llf.addLen(line.length());
        return line;
    }
    /**
     * Process a paragraph
     * @param paragraph the paragraph to process
     * @param offset the offset at the start of the paragraph
     * @return the paragraph text *after* processing
     */
    String processParagraph( String paragraph, int offset ) throws Exception
    {
        int paraOffset = offset;
        StringBuilder sb = new StringBuilder();
        Range r=null;
        ParaFormat pf=null;
        boolean usingDefaultPName = false;
        // apply para formats
        for ( int i=0;i<paraFormats.size();i++ )
        {
            pf = paraFormats.get(i);
            if ( paragraph.startsWith(pf.startTag) 
                && paragraph.endsWith(pf.endTag) )
            {
                r = new Range(pf.prop);
                int sTagLen = pf.startTag.length();
                int eTagLen = pf.endTag.length();
                paragraph = paragraph.substring(sTagLen,
                    paragraph.length()-eTagLen);
                break;
            }
        }
        String[] lines = paragraph.split("\n");
        if ( r == null )
        {
            // look for setext headings
            if ( lines.length>=2 )
            {
                for ( int i=1;i<lines.length;i++ )
                {
                    for ( int j=0;j<setexts.size();j++ )
                    {
                        Setext st = setexts.get(j);
                        String underline = lines[i];
                        while ( underline.startsWith(st.tag) )
                            underline = underline.substring(st.tag.length() );
                        if ( underline.length()==0 )
                        {
                            // match: remove underline
                            String[] newLines = new String[lines.length-1];
                            for ( int k=0;k<i;k++ )
                                newLines[k] = lines[k];
                            for ( int k=i+1;k<lines.length;k++ )
                                newLines[k] = lines[k-1];
                            lines = newLines;
                            r = new Range( st.prop );
                        }
                    }
                }
            }
        }
        // provide default paragraph property if none provided
        if ( r == null )
        {
            JSONObject jObj = (JSONObject)dialect.get("paragraph");
            String prop = (String)jObj.get("prop");
            if ( prop.length()==0 )
                prop = "p";
            r = new Range(prop);
            usingDefaultPName = true;
        }
        // add it *before* the text: r must be defined by now
        corcodeDefault.add(r);
        // now process individual lines
        LastLineFormat llf = new LastLineFormat(null);
        boolean hasOnlyLineFormats = true;
        for ( int i=0;i<lines.length;i++ )
        {
            String line = processLine(lines[i],offset,llf);
            if ( llf.range == null )
                hasOnlyLineFormats = false;
            sb.append(line);
            // restore NL
            if ( i < lines.length-1 )
            {
                sb.append("\n");
                // include trailing NL in format
                if ( llf.range != null )
                    llf.addLen(1);
            }
            offset = paraOffset + sb.length();
        }
        // avoid nesting of paragraph formats
        if ( hasOnlyLineFormats && usingDefaultPName )
        {
            corcodeDefault.remove(r);
            r = null;
        }
        // now set the paragraph range's offset and length
        if ( r != null )
        {
            r.setOffset( paraOffset );
            r.setLen(sb.length());
        }
        return sb.toString();
    }
    /**
     * Process a section
     * @param section the raw section input
     * @param offset the offset at which this section begins
     * @return the plain text section
     */
    String processSection( String section, int offset ) throws MMLException
    {
        int sectionOffset = offset;
        if ( section.length() > 0 )
        {
            StringBuilder sb = new StringBuilder();
            int index = section.indexOf("\n");
            String sectionName = "section";    // default section name
            if ( index > 0 )
            {
                // read section name
                String name = section.substring(0,index);
                if ( name.startsWith("{")&& name.endsWith("}") && name.indexOf(" ")==-1)
                {
                    sectionName = name.substring(1,name.length()-1);
                    section = section.substring(index+1);
                }
            }
            Range r = new Range(sectionName);
            try
            {
                if ( !sectionName.equals("section") || writeAnonSections )
                    corcodeDefault.add(r);
                String[] paragraphs = section.split("\n\n");
                for ( int i=0;i<paragraphs.length;i++ )
                {
                    sb.append(processParagraph(paragraphs[i],offset));
                    if ( i < paragraphs.length-1 )
                    {
                        sb.append("\n\n");
                    }
                    offset = sectionOffset + sb.length();
                }
                r.setOffset( sectionOffset );
                r.setLen(sb.length());
                return sb.toString();
            }
            catch ( Exception e )
            {
                throw new MMLException(e);
            }
        }
        else
            return "";
    }
    /**
     * Remove whistespace at line-end or completely on lines that are only 
     * white space, leaving one NL only
     * @param text the text to clean
     * @return the cleaned text
     */
    String cleanWhitespace( String text )
    {
        String[] lines = text.split("\n");
        for ( int i=0;i<lines.length;i++ )
        {
            // always remove trailing whitespace
             while ( lines[i].endsWith(" ")||lines[i].endsWith("\t") )
                 lines[i] = lines[i].substring(0,lines[i].length()-1 );
             if ( isWhitespace(lines[i]) )
             {
                 // remove leading whitespace also
                 char token = lines[i].charAt(0);
                 while ( lines[i].length()>0 && token == '\t' || token == ' ' )
                 {
                     lines[i] = lines[i].substring(1);
                     if ( lines[i].length() > 0 )
                         token = lines[i].charAt(0);
                 }
             }
        }
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<lines.length;i++ )
        {
            sb.append(lines[i]);
            if ( i < lines.length-1 )
                sb.append("\n");
        }
        return sb.toString();
    }
    /**
     * Parse the text of an MML file. Append the results to cortex and corcode
     * @param mml the mml to parse
     */
    void parseMML( String mml ) throws MMLException
    {
        mml = cleanWhitespace(mml);
        String[] sections = mml.split("\n\n\n"); 
        int offset = 0;
        for ( int i=0;i<sections.length;i++ )
        {
            String section = processSection(sections[i],offset);
            cortex.append(section);
            if ( i < sections.length-1 )
            {
                cortex.append("\n\n\n");
                offset += 3;
            }
            offset += section.length();
        }
    }
    /**
     * Change a file name suffix
     * @param name the original file basefilename.oldSuffix
     * @param newSuffix the new suffix *minus* the "."
     * @return the new file name with the basefilename+dot+newSuffix
     */
    String swapSuffix( String name, String newSuffix )
    {
        int index = name.lastIndexOf(".");
        if ( index != -1 )
            name = name.substring(0,index);
        return name+"."+newSuffix;
    }
    /**
     * Compute the relative path from the value on the directory stack
     * @return a relative path from target dir to current file
     */
    String relPath()
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<dirs.size();i++ )
        {
            String part = dirs.get(i);
            if ( sb.length()>0 )
                sb.append("/");
            sb.append(part);
        }
        return sb.toString();
    }
    /**
     * Write a string to a single file
     * @param dir the parent dir of the file
     * @param fName the name of the actual file
     * @param data the data to write as a string
     * @throws Exception 
     */
    void writeFile( File dir, String fName, String data ) throws Exception
    {
        File dest = new File( dir, fName );
        if ( !dest.exists() || dest.delete() )
            dest.createNewFile();
        else
            throw new Exception("Couldn't create file "+fName);
        FileOutputStream fos = new FileOutputStream( dest );
        byte[] bytes = data.getBytes("UTF-8");
        fos.write( bytes );
        fos.close();
    }
    /**
     * Process a file. Make a destination dir
     * @param f the source file to strip
     */
    void processFile( File f ) throws Exception
    {
        // reinit cortex, corcode
        cortex = new StringBuilder();
        corcodeDefault = new STILDocument(this.style);
        corcodePages = new STILDocument(this.style);
        if ( dest == null )
        {
            dest = new File( "dest" );
            if ( dest.exists() )
                dest.delete();
            dest.mkdir();
            textDir = new File(dest,"text");
            stilDir = new File(dest,"stil");
            textDir.mkdir();
            stilDir.mkdir();
        }
        // OK so now we know where everything goes
        if ( f.getName().endsWith(".md"))
        {
            String text = readFile(f);
            if ( f.getName().startsWith("02-DEC-1865-"))
                System.out.println("Error");
            parseMML( text );
            String corCodeFileDefault = swapSuffix(f.getName(), "corcode-default.json");
            String corCodeFilePages = swapSuffix(f.getName(), "corcode-pages.json");
            String cortexFile = swapSuffix( f.getName(), "cortex.txt");
            String path = relPath();
            File ccDir = new File(stilDir,path);
            if ( !ccDir.exists() )
                ccDir.mkdirs();
            File ctDir = new File(textDir,path);
            if ( !ctDir.exists() )
                ctDir.mkdirs();
            corcodeDefault.trimTo(cortex.length());
            writeFile( ccDir, corCodeFileDefault, corcodeDefault.toJSON());
            if ( !corcodePages.isEmpty() )
                writeFile( ccDir, corCodeFilePages, corcodePages.toJSON());
            writeFile( ctDir, cortexFile, cortex.toString());
        }
    }
    /**
     * Look for files to process in a directory
     * @param dir the dir to search
     * @throws Exception 
     */
    void processDirectory( File dir ) throws Exception
    {
        File[] files = dir.listFiles();
        dirs.push(dir.getName());
        for ( int i=0;i<files.length;i++ )
        {
            if ( files[i].isDirectory() )
                processDirectory(files[i]);
            else
                processFile(files[i]);
        }
        dirs.pop();
    }
    /**
     * Check commandline arguments
     * @param args the argument list
     * @param mmls an MMLStripper object
     * @return true if the arguments were usable
     */
    static boolean checkArgs( String[] args, MMLStripper mmls )
    {
        boolean sane = false;
        if ( args.length >= 3 )
        {
            for ( int i=0;i<args.length;i++ )
            {
                if ( args[i].length()==2 && args[i].charAt(0)=='-' )
                {
                    switch( args[i].charAt(1) )
                    {
                        case 'd':// dialect follows
                            if ( args.length > i+1 )
                                sane = mmls.readDialectFile(args[i+1]);
                            break;
                        case 's':// style follows
                            if ( args.length > i+1 )
                                mmls.style = args[i+1];
                            break;
                        case 'a':   // write anonymous sections
                            mmls.writeAnonSections = true;
                            break;
                        default:
                            System.out.println("Invalid option "+args[i].charAt(1));
                            sane = false;
                            break;
                    }
                }
            }
            mmls.target = new File(args[args.length-1]);
            if ( mmls.style == null )
                mmls.style = "default";
            if ( sane && mmls.target.exists() )
                sane = true;
            else
            {
                if ( sane )
                    System.out.println(args[args.length-1]+" does not exist");
                sane = false;
            }
        }
        return sane;
    }
    public static void main( String[] args )
    {
        MMLStripper mmls = new MMLStripper();
        if ( checkArgs(args,mmls) )
        {
            try
            {
                mmls.dirs = new Stack<String>();
                if ( mmls.target.isFile() )
                    mmls.processFile( mmls.target );
                else
                    mmls.processDirectory(mmls.target);
            }
            catch ( Exception e )
            {
                e.printStackTrace(System.out);
            }
        }
        else
            System.out.println("Usage: java MMLStripper [-a] [-s style] -d dialect.json <folder|file>");
    }
}
