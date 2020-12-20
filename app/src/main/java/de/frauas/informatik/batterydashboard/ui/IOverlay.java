package de.frauas.informatik.batterydashboard.ui;

import android.view.ViewGroup;

/**
 * Interface that should be implemented by any class used as a "window" of our application. </br></br>
 *
 * The method getContent() is needed to manipulate views: </br>
 * <b>HOW TO FIND VIEWS BY ID:</b> </br>
 *  You can use getContent() on any class implementing the overlay interface to retrieve the base ViewGroup object of the layout from UiService.</br>
 *  Then you can use findViewById(R.id.XY) on the returned object ;)
 *
 * @see Dashboard see Dashboard as an example that implements this!
 *
 * @author filzinge@stud.fra-uas.de
 */

public interface IOverlay {
    ViewGroup getContent();
}
