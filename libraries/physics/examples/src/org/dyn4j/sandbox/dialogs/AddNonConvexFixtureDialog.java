/*
 * Copyright (c) 2010-2016 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.sandbox.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.sandbox.controls.BottomButtonPanel;
import org.dyn4j.sandbox.panels.FixturePanel;
import org.dyn4j.sandbox.panels.NonConvexPolygonPanel;
import org.dyn4j.sandbox.panels.TransformPanel;
import org.dyn4j.sandbox.resources.Messages;

/**
 * Dialog to add a new fixture to an existing body.
 * @author William Bittle
 * @version 1.0.4
 * @since 1.0.0
 */
public class AddNonConvexFixtureDialog extends JDialog implements ActionListener {
	/** The version id */
	private static final long serialVersionUID = -1809110047704548125L;
	
	/** The dialog canceled flag */
	private boolean canceled = true;

	/** The fixture config panel */
	private FixturePanel pnlFixture;
	
	/** The transform config panel */
	private TransformPanel pnlTransform;
	
	/** The non-convex polygon panel */
	private NonConvexPolygonPanel pnlPolygon;

	/** The fixture used during configuration */
	private BodyFixture fixture;
	
	/**
	 * Full constructor.
	 * @param owner the dialog owner
	 * @param icon the icon image
	 */
	private AddNonConvexFixtureDialog(Window owner, Image icon) {
		super(owner, Messages.getString("dialog.fixture.nonconvex.title"), ModalityType.APPLICATION_MODAL);
		
		if (icon != null) {
			this.setIconImage(icon);
		}
		
		this.pnlPolygon = new NonConvexPolygonPanel();
		
		// create a text pane for the local transform tab
		JTextPane lblText = new JTextPane();
		lblText = new JTextPane();
		lblText.setContentType("text/html");
		lblText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		lblText.setText(Messages.getString("label.transform.warning"));
		lblText.setEditable(false);
		lblText.setPreferredSize(new Dimension(350, 120));
		
		// have to create it with an arbitrary shape
		this.fixture = new BodyFixture(Geometry.createCircle(1.0));
		this.fixture.setUserData(MessageFormat.format(Messages.getString("dialog.fixture.add.name.default"), AddConvexFixtureDialog.N));
		this.pnlFixture = new FixturePanel(this.fixture);
		this.pnlTransform = new TransformPanel(lblText);
		
		JTabbedPane tabs = new JTabbedPane();
		
		tabs.setBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0));
		tabs.addTab(Messages.getString("dialog.fixture.tab.shape"), this.pnlPolygon);
		tabs.addTab(Messages.getString("dialog.fixture.tab.fixture"), this.pnlFixture);
		tabs.addTab(Messages.getString("dialog.fixture.tab.transform"), this.pnlTransform);
		
		JButton btnCancel = new JButton(Messages.getString("button.cancel"));
		JButton btnAdd = new JButton(Messages.getString("button.add"));
		btnCancel.setActionCommand("cancel");
		btnAdd.setActionCommand("add");
		btnCancel.addActionListener(this);
		btnAdd.addActionListener(this);
		
		JPanel pnlButtons = new BottomButtonPanel();
		pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		pnlButtons.add(btnCancel);
		pnlButtons.add(btnAdd);

		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		container.add(tabs, BorderLayout.CENTER);
		container.add(pnlButtons, BorderLayout.PAGE_END);
		
		this.pack();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		// check the action command
		if ("cancel".equals(event.getActionCommand())) {
			// if its canceled then set the canceled flag and
			// close the dialog
			this.setVisible(false);
			this.canceled = true;
		} else {
			// check the shape panel's input
			if (this.pnlPolygon.isValidInput()) {
				// check the fixture input
				if (this.pnlFixture.isValidInput()) {
					// check the transform input
					if (this.pnlTransform.isValidInput()) {
						// if its valid then close the dialog
						this.canceled = false;
						this.setVisible(false);
					} else {
						this.pnlTransform.showInvalidInputMessage(this);
					}
				} else {
					this.pnlFixture.showInvalidInputMessage(this);
				}
			} else {
				// if its not valid then show an error message
				this.pnlPolygon.showInvalidInputMessage(this);
			}
		}
	}
	
	/**
	 * Shows a decomposition dialog.
	 * <p>
	 * Returns null if the user canceled or closed the dialog.
	 * @param owner the dialog owner
	 * @param icon the icon image
	 * @return List&lt;BodyFixture&gt;
	 */
	public static final List<BodyFixture> show(Window owner, Image icon) {
		AddNonConvexFixtureDialog dialog = new AddNonConvexFixtureDialog(owner, icon);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
		// control returns to this method when the dialog is closed
		
		// check the canceled flag
		if (!dialog.canceled) {
			// get the list of convex shapes
			List<Convex> shapes = dialog.pnlPolygon.getShapes();
			
			// get the general fixture (properties will be copied into all fixtures created)
			BodyFixture fixture = dialog.fixture;
			
			// apply any local transform
			Vector2 tx = dialog.pnlTransform.getTranslation();
			double a = dialog.pnlTransform.getRotation();
			
			List<BodyFixture> fixtures = new ArrayList<BodyFixture>(shapes.size());
			int i = 1;
			// create a fixture for each shape
			for (Convex convex : shapes) {
				BodyFixture bf = new BodyFixture(convex);
				
				// apply the local transform
				if (!tx.isZero()) {
					convex.translate(tx);
				}
				if (a != 0.0) {
					convex.rotateAboutCenter(a);
				}
				
				bf.setDensity(fixture.getDensity());
				bf.filter =(fixture.filter);
				bf.setFriction(fixture.getFriction());
				bf.setRestitution(fixture.getRestitution());
				bf.setSensor(fixture.isSensor());
				bf.setUserData(fixture.getUserData() + "_" + i);
				
				fixtures.add(bf);
				i++;
			}
			
			// increment the fixture number
			synchronized (AddConvexFixtureDialog.class) {
				AddConvexFixtureDialog.N++;
			}
			
			return fixtures;
		}
		
		// if it was canceled then return null
		return null;
	}
}
