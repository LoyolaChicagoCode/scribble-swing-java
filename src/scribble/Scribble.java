package scribble;

// This example is from the book "Java in a Nutshell, Second Edition".
// Written by David Flanagan.  Copyright (c) 1997 O'Reilly & Associates.
// You may distribute this source code for non-commercial purposes only.
// You may study, modify, and use this example for any purpose, as long as
// this notice is retained.  Note that this example is provided "as is",
// WITHOUT WARRANTY of any kind either expressed or implied.

// Updated to Swing by Konstantin L\uFFFDufer <laufer@acm.org> July 2002
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This class is a custom component that supports scribbling.  It also has
 * a popup menu that allows the scribble color to be set and provides access
 * to printing, cut-and-paste, and file loading and saving facilities.
 * Note that it extends Component rather than Canvas, making it "lightweight."
 */
class Scribble extends JComponent
    implements ActionListener, MouseListener, MouseMotionListener, Printable {
    
    protected short last_x, last_y;                // Coordinates of last click.
    protected short first_x, first_y;
    short width, height;
    Point point=new Point();


    final static Color bg = Color.white;
    final static Color fg = Color.black;
    final static Color red = Color.red; 
    final static Color white = Color.white;

    protected ArrayList lines = new ArrayList(256);  // Store the scribbles.
    protected Color current_color = Color.black;   // Current drawing color.
    protected JPopupMenu popup;                     // The popup menu.
    protected JFrame frame;                         // The frame we are within.
    
    /** This constructor requires a Frame and a desired size */
    public Scribble(JFrame frame, int width, int height) {
        this.frame = frame;
        this.setPreferredSize(new Dimension(width, height));
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

    // Create the popup menu using a loop.  Note the separation of menu
    // "action command" string from menu label.  Good for internationalization.
    String[] labels = new String[] {
      "Clear", "Print", "Save", "Load", "Cut", "Copy", "Paste" };
    String[] commands = new String[] {
      "clear", "print", "save", "load", "cut", "copy", "paste" };
    popup = new JPopupMenu();                   // Create the menu
    for(int i = 0; i < labels.length; i++) {
      JMenuItem mi = new JMenuItem(labels[i]);   // Create a menu item.
      mi.setActionCommand(commands[i]);        // Set its action command.
      mi.addActionListener(this);              // And its action listener.
      popup.add(mi);                           // Add item to the popup menu.
    }
    JMenu colors = new JMenu("Color");           // Create a submenu.
    popup.add(colors);                         // And add it to the popup.
    String[] colornames = new String[] { "Black", "Red", "Green", "Blue"};
    for(int i = 0; i < colornames.length; i++) {
      JMenuItem mi = new JMenuItem(colornames[i]);  // Create the submenu items
      mi.setActionCommand(colornames[i]);         // in the same way.
      mi.addActionListener(this);
      colors.add(mi);
    }
    // Finally, register the popup menu with the component it appears over
    this.add(popup);
  }

  /** This is the ActionListener method invoked by the popup menu items */
  public void actionPerformed(ActionEvent event) {
    // Get the "action command" of the event, and dispatch based on that.
    // This method calls a lot of the interesting methods in this class.
    String command = event.getActionCommand();
    if (command.equals("clear")) clear();
    else if (command.equals("print")) print();
    else if (command.equals("save")) save();
    else if (command.equals("load")) load();
    else if (command.equals("cut")) cut();
    else if (command.equals("copy")) copy();
    else if (command.equals("paste")) paste();
    else if (command.equals("Black")) current_color = Color.black;
    else if (command.equals("Red")) current_color = Color.red;
    else if (command.equals("Green")) current_color = Color.green;
    else if (command.equals("Blue")) current_color = Color.blue;
  }

  /** Draw all the saved lines of the scribble, in the appropriate colors */
  public void paint(Graphics g) {
      for(int i = 0; i < lines.size(); i++) {
          Line l = (Line)lines.get(i);
          g.setColor(l.color);
          g.drawLine(l.x1, l.y1, l.x2, l.y2);
      }
  }

  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup.show(e.getComponent(), e.getX(), e.getY());
    } else {
      last_x = (short)e.getX(); last_y = (short)e.getY(); // Save position.
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  public void mouseDragged(MouseEvent e) {
    Graphics g = getGraphics();                     // Object to draw with.
    g.setColor(current_color);                      // Set the current color.
    g.drawLine(last_x, last_y, e.getX(), e.getY()); // Draw this line
    lines.add(new Line(last_x, last_y,       // and save it, too.
                              (short) e.getX(), (short)e.getY(),
                              current_color));
    last_x = (short) e.getX();  // Remember current mouse coordinates.
    last_y = (short) e.getY();
  }
  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }

  /** Clear the scribble.  Invoked by popup menu */
  void clear() {
    lines.clear();               // Throw out the saved scribble
    repaint();                   // and redraw everything.
  }

public void print(){
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);

        if (printJob.printDialog()) {
            try {
                printJob.print();  
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        return Printable.PAGE_EXISTS;
    }


  /** Print out the scribble.  Invoked by popup menu. */
  void print_old() {
    // Obtain a PrintJob object.  This posts a Print dialog.
    // printprefs (created below) stores user printing preferences.
    Toolkit toolkit = this.getToolkit();
    PrintJob job = toolkit.getPrintJob(frame, "Scribble", printprefs);
    printprefs.toString();

    // If the user clicked Cancel in the print dialog, then do nothing.
    //if (job == null) return;

    // Get a Graphics object for the first page of output.
    Graphics page = job.getGraphics();

    // Check the size of the scribble component and of the page.
    Dimension size = this.getSize();
    Dimension pagesize = job.getPageDimension();

    // Center the output on the page.  Otherwise it would be
    // be scrunched up in the upper-left corner of the page.
    page.translate((pagesize.width - size.width)/2,
                   (pagesize.height - size.height)/2);

    // Draw a border around the output area, so it looks neat.
    page.drawRect(-1, -1, size.width+1, size.height+1);

    // Set a clipping region so our scribbles don't go outside the border.
    // On-screen this clipping happens automatically, but not on paper.
    page.setClip(0, 0, size.width, size.height);

    // Print this Scribble component.  By default this will just call paint().
    // This method is named print(), too, but that is just coincidence.
    this.print(page);

    // Finish up printing.
    page.dispose();   // End the page--send it to the printer.
    job.end();        // End the print job.
  }

  /** This Properties object stores the user print dialog settings. */
  private static Properties printprefs = new Properties();

  /**
   * The DataFlavor used for our particular type of cut-and-paste data.
   * This one will transfer data in the form of a serialized Vector object.
   * Note that in Java 1.1.1, this works intra-application, but not between
   * applications.  Java 1.1.1 inter-application data transfer is limited to
   * the pre-defined string and text data flavors.
   */
  public static final DataFlavor dataFlavor =
      new DataFlavor(ArrayList.class, "ScribbleListOfLines");

  /**
   * Copy the current scribble and store it in a SimpleSelection object
   * (defined below).  Then put that object on the clipboard for pasting.
   */
  public void copy() {
    // Get system clipboard
    Clipboard c = this.getToolkit().getSystemClipboard();
    // Copy and save the scribble in a Transferable object
    SimpleSelection s = new SimpleSelection(lines.clone(), dataFlavor);
    // Put that object on the clipboard
    c.setContents(s, s);
  }

  /** Cut is just like a copy, except we erase the scribble afterwards */
  public void cut() { copy(); clear();  }

  /**
   * Ask for the Transferable contents of the system clipboard, then ask that
   * object for the scribble data it represents.  If either step fails, beep!
   */
  public void paste() {
    Clipboard c = this.getToolkit().getSystemClipboard();  // Get clipboard.
    Transferable t = c.getContents(this);                  // Get its contents.
    if (t == null) {              // If there is nothing to paste, beep.
      this.getToolkit().beep();
      return;
    }
    try {
      // Ask for clipboard contents to be converted to our data flavor.
      // This will throw an exception if our flavor is not supported.
      ArrayList newlines = (ArrayList) t.getTransferData(dataFlavor);
      // Add all those pasted lines to our scribble.
      for(int i = 0; i < newlines.size(); i++)
        lines.add(newlines.get(i));
      // And redraw the whole thing
      repaint();
    }
    catch (UnsupportedFlavorException e) {
      this.getToolkit().beep();   // If clipboard has some other type of data
    }
    catch (Exception e) {
      this.getToolkit().beep();   // Or if anything else goes wrong...
    }
  }

  /**
   * This nested class implements the Transferable and ClipboardOwner
   * interfaces used in data transfer.  It is a simple class that remembers a
   * selected object and makes it available in only one specified flavor.
   */
  static class SimpleSelection implements Transferable, ClipboardOwner {
    protected Object selection;    // The data to be transferred.
    protected DataFlavor flavor;   // The one data flavor supported.
    public SimpleSelection(Object selection, DataFlavor flavor) {
      this.selection = selection;  // Specify data.
      this.flavor = flavor;        // Specify flavor.
    }

    /** Return the list of supported flavors.  Just one in this case */
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { flavor };
    }
    /** Check whether we support a specified flavor */
    public boolean isDataFlavorSupported(DataFlavor f) {
      return f.equals(flavor);
    }
    /** If the flavor is right, transfer the data (i.e. return it) */
    public Object getTransferData(DataFlavor f)
         throws UnsupportedFlavorException {
      if (f.equals(flavor)) return selection;
      else throw new UnsupportedFlavorException(f);
    }

    /** This is the ClipboardOwner method.  Called when the data is no
     *  longer on the clipboard.  In this case, we don't need to do much. */
    public void lostOwnership(Clipboard c, Transferable t) {
      selection = null;
    }
  }

  /**
   * Prompt the user for a filename, and save the scribble in that file.
   * Serialize the vector of lines with an ObjectOutputStream.
   * Compress the serialized objects with a GZIPOutputStream.
   * Write the compressed, serialized data to a file with a FileOutputStream.
   * Don't forget to flush and close the stream.
   */
  public void save() {
    // Create a file dialog to query the user for a filename.
    JFileChooser f = new JFileChooser();
    int result = f.showSaveDialog(frame);         // Display the dialog and block.
    if (result != JFileChooser.APPROVE_OPTION) {
      return;
    }
    String filename = f.getSelectedFile().getPath();    // Get the user's response
    if (filename != null) {          // If user didn't click "Cancel".
      try {
        // Create the necessary output streams to save the scribble.
        FileOutputStream fos = new FileOutputStream(filename); // Save to file
        GZIPOutputStream gzos = new GZIPOutputStream(fos);     // Compressed
        ObjectOutputStream out = new ObjectOutputStream(gzos); // Save objects
        out.writeObject(lines);      // Write the entire Vector of scribbles
        out.flush();                 // Always flush the output.
        out.close();                 // And close the stream.
      }
      // Print out exceptions.  We should really display them in a dialog...
      catch (IOException e) { System.out.println(e); }
    }
  }

  /**
   * Prompt for a filename, and load a scribble from that file.
   * Read compressed, serialized data with a FileInputStream.
   * Uncompress that data with a GZIPInputStream.
   * Deserialize the vector of lines with a ObjectInputStream.
   * Replace current data with new data, and redraw everything.
   */
  public void load() {
    // Create a file dialog to query the user for a filename.
    JFileChooser f = new JFileChooser();
    int result = f.showOpenDialog(frame); // Display the dialog and block.
    if (result != JFileChooser.APPROVE_OPTION) {
      return;
    }
    String filename = f.getSelectedFile().getPath();    // Get the user's response
    if (filename != null) {           // If user didn't click "Cancel".
      try {
        // Create necessary input streams
        FileInputStream fis = new FileInputStream(filename); // Read from file
        GZIPInputStream gzis = new GZIPInputStream(fis);     // Uncompress
        ObjectInputStream in = new ObjectInputStream(gzis);  // Read objects
        // Read in an object.  It should be a vector of scribbles
        ArrayList newlines = (ArrayList) in.readObject();
        in.close();                    // Close the stream.
        lines = newlines;              // Set the Vector of lines.
        repaint();                     // And redisplay the scribble.
      }
      // Print out exceptions.  We should really display them in a dialog...
      catch (Exception e) { System.out.println(e); }
    }
  }

  /** A class to store the coordinates and color of one scribbled line.
   *  The complete scribble is stored as a Vector of these objects */
  static class Line implements Serializable {
    public short x1, y1, x2, y2;
    public Color color;
    public Line(short x1, short y1, short x2, short y2, Color c) {
      this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.color = c;
    }
  }
}
