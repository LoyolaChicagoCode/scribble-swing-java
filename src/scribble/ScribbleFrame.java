package scribble;

// This example is from the book "Java in a Nutshell, Second Edition".
// Written by David Flanagan.  Copyright (c) 1997 O'Reilly & Associates.
// You may distribute this source code for non-commercial purposes only.
// You may study, modify, and use this example for any purpose, as long as
// this notice is retained.  Note that this example is provided "as is",
// WITHOUT WARRANTY of any kind either expressed or implied.

// Updated to Swing by Konstantin L\uFFFDufer <laufer@acm.org> July 2002

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

/**
 * This class places a Scribble component in a ScrollPane container,
 * puts the ScrollPane in a window, and adds a simple pulldown menu system.
 * The menu uses menu shortcuts.  Events are handled with anonymous classes.
 */
public class ScribbleFrame extends JFrame {
  /** A very simple main() method for our program. */
  public static void main(String[] args) { new ScribbleFrame(); }

  /** Remember # of open windows so we can quit when last one is closed */
  protected static int num_windows = 0;

  /** Create a Frame, Menu, and ScrollPane for the scribble component */
  public ScribbleFrame() {
    super("ScribbleFrame");                  // Create the window.
    num_windows++;                           // Count it.

    Scribble scribble;
    scribble = new Scribble(this, 500, 500); // Create a bigger scribble area.
    JScrollPane pane = new JScrollPane(scribble);      // Create a ScrollPane.
    pane.setPreferredSize(new Dimension(300, 300));
    this.getContentPane().add(pane);                // Add it to the frame.

    JMenuBar menubar = new JMenuBar();         // Create a menubar.
    this.setJMenuBar(menubar);                // Add it to the frame.
    JMenu file = new JMenu("File");            // Create a File menu.
    menubar.add(file);                       // Add to menubar.

    // Create three menu items, with menu shortcuts, and add to the menu.
    JMenuItem n, c, q, p;
    file.add(n = new JMenuItem("New Window", KeyEvent.VK_N));
    file.add(c = new JMenuItem("Close Window", KeyEvent.VK_W));
    file.add(p = new JMenuItem("Print Window", KeyEvent.VK_P));
    file.addSeparator();                     // Put a separator in the menu
    file.add(q = new JMenuItem("Quit", KeyEvent.VK_Q));

    // Create and register action listener objects for the three menu items.
    n.addActionListener(new ActionListener() {     // Open a new window
      public void actionPerformed(ActionEvent e) { new ScribbleFrame(); }
    });
    c.addActionListener(new ActionListener() {     // Close this window.
      public void actionPerformed(ActionEvent e) { close(); }
    });
    p.addActionListener(new ActionListener() {     // Close this window.
      public void actionPerformed(ActionEvent e) { printWindow(); }
    });

    q.addActionListener(new ActionListener() {     // Quit the program.
      public void actionPerformed(ActionEvent e) { System.exit(0); }
    });

    // Another event listener, this one to handle window close requests.
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { close(); }
    });

    // Set the window size and pop it up.
    this.pack();
    this.setVisible(true);
  }

  /** Close a window.  If this is the last open window, just quit. */
  void close() {
    if (--num_windows == 0) System.exit(0);
    else this.dispose();
  }

  /** Print the current window. */
  void printWindow() {
    Properties printprefs = new Properties();
    Toolkit toolkit = this.getToolkit();
    PrintJob job = toolkit.getPrintJob(this, "Scribble", printprefs);

    if (job == null) return;

    Graphics page = job.getGraphics();

    Dimension size = this.getSize();
    Dimension pagesize = job.getPageDimension();

    page.translate((pagesize.width - size.width)/2,
                   (pagesize.height - size.height)/2);

    page.drawRect(-1, -1, size.width+1, size.height+1);

    page.setClip(0, 0, size.width, size.height);

    this.print(page);

    page.dispose();
    job.end();
  }
}









