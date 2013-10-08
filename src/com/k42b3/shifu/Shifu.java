/**
 * shifu
 * A application to solve conversion, arithmetic and logical challenges. For the 
 * current version and more informations visit <https://github.com/k42b3/shifu>
 * 
 * Copyright (c) 2013 Christoph Kappestein <k42b3.x@gmail.com>
 * 
 * This file is part of Shifu. Shifu is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or at any later version.
 * 
 * Shifu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Shifu. If not, see <http://www.gnu.org/licenses/>.
 */

package com.k42b3.shifu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Shifu
 *
 * @author  Christoph Kappestein <k42b3.x@gmail.com>
 * @license http://www.gnu.org/licenses/gpl.html GPLv3
 * @link    http://shifu.k42b3.com
 */
public class Shifu extends JFrame
{
	public static final String VERSION = "0.0.4 beta";

	private Connection connection;
	private DefaultTableModel resultModel;

	public Shifu()
	{
		this.setTitle("Shifu " + VERSION);
		this.setLocation(100, 100);
		this.setSize(360, 220);
		this.setMinimumSize(this.getSize());
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.connect();

		JTabbedPane tp = new JTabbedPane();
		tp.setFocusable(false);
		tp.addTab("Processor", new Processor(connection));
		tp.addTab("Memory", new Memory(connection));
		tp.addTab("Results", this.buildResultPanel());

		tp.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e)
			{
				loadResults();
			}

		});

		this.add(tp, BorderLayout.CENTER);
	}
	
	protected void connect()
	{
		try 
		{
			Class.forName("org.sqlite.JDBC");

			connection = DriverManager.getConnection("jdbc:sqlite:results.db");
			
			// check whether structure exists
			Statement stmt = connection.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS results (";
			sql+= "id INTEGER PRIMARY KEY AUTOINCREMENT,";
			sql+= "challenge VARCHAR(64) NOT NULL,";
			sql+= "answer INTEGER NOT NULL,";
			sql+= "correct TINYINT NOT NULL,";
			sql+= "date DATETIME NOT NULL";
			sql+= ");";
			
			stmt.executeUpdate(sql);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void loadResults()
	{
		try
		{
			Statement stmt = connection.createStatement();
			ResultSet result = stmt.executeQuery("SELECT `challenge`, `answer`, `correct`, `date` FROM `results` ORDER BY `date` DESC");
			
			resultModel.setRowCount(0);
			
			while(result.next())
			{
				Object[] row = {
					result.getString("challenge"), 
					result.getInt("answer"),
					result.getString("correct"),
					result.getString("date")
				};

				resultModel.addRow(row);
			}
		}
		catch(SQLException e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected Component buildResultPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		
		resultModel = new DefaultTableModel();
		resultModel.addColumn("Challenge");
		resultModel.addColumn("Answer");
		resultModel.addColumn("Correct");
		resultModel.addColumn("Date");

		JTable tblResult = new JTable(resultModel){

			public boolean isCellEditable(int row, int column)
			{
				return false;
			}

		};
		
		TableCellRenderer cellRenderer = new CellRendererResult();
		tblResult.getColumn("Challenge").setCellRenderer(cellRenderer);
		tblResult.getColumn("Answer").setCellRenderer(cellRenderer);
		tblResult.getColumn("Date").setCellRenderer(cellRenderer);
		
		tblResult.removeColumn(tblResult.getColumn("Correct"));

		panel.add(new JScrollPane(tblResult), BorderLayout.CENTER);
		
		return panel;
	}

	private class CellRendererResult implements TableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			JLabel comp = new JLabel(value.toString());
			comp.setOpaque(true);
			
			if(resultModel.getValueAt(row, 2).equals("1"))
			{
				comp.setBackground(Color.GREEN);
			}
			else
			{
				comp.setBackground(Color.RED);
			}

			return comp;
		}
	}
}
