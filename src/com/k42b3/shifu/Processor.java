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
 * Processor
 *
 * @author  Christoph Kappestein <k42b3.x@gmail.com>
 * @license http://www.gnu.org/licenses/gpl.html GPLv3
 * @link    http://shifu.k42b3.com
 */
public class Processor extends JTabbedPane
{
	public static final int TYPE_CONVERT_BINARY = 0x0;
	public static final int TYPE_CONVERT_OCTAL = 0x1;
	public static final int TYPE_CONVERT_HEX = 0x2;
	public static final int TYPE_MATH_ADDITION = 0x4;
	public static final int TYPE_MATH_SUBTRACT = 0x5;
	public static final int TYPE_MATH_DIVIDE = 0x6;
	public static final int TYPE_MATH_MULTIPLY = 0x7;
	public static final int TYPE_MATH_MODULO = 0x8;
	public static final int TYPE_LOGIC_AND = 0x9;
	public static final int TYPE_LOGIC_OR = 0xA;
	public static final int TYPE_LOGIC_XOR = 0xB;
	
	private int maxInteger = 32;
	
	private ArrayList<Integer> types = new ArrayList<Integer>();
	private String challenge;
	private int answer;
	
	private JLabel lblChallenge;
	private JTextField txtAnswer;
	private JButton btnSolve;
	private JButton btnSkip;
	private JButton btnAnswer;
	
	private Connection connection;
	private HashMap<Integer, JCheckBox> settingTypes = new HashMap<Integer, JCheckBox>();

	public Processor(Connection connection)
	{
		this.connection = connection;

		this.setFocusable(false);
		this.addTab("Challenge", buildChallengePanel());
		this.addTab("Settings", buildSettingPanel());

		// connect to db
		this.connect();

		// build first challenge
		this.nextChallenge();
	}
	

	protected void nextChallenge()
	{
		int index = (int) (types.size() * Math.random());
		int type = types.get(index);
		
		int answer;
		String challenge;
		int a1;
		int a2;
		
		switch(type)
		{
			case TYPE_MATH_ADDITION:
				a1 = getRandomInt();
				a2 = getRandomInt();
				answer = a1 + a2;
				challenge = a1 + " + " + a2;
				break;

			case TYPE_MATH_SUBTRACT:
				a1 = getRandomInt();
				a2 = getRandomInt();
				answer = a1 - a2;
				challenge = a1 + " - " + a2;
				break;

			case TYPE_MATH_DIVIDE:
				a1 = getRandomInt();
				a2 = getRandomInt();

				if(a2 == 0)
				{
					nextChallenge();
					return;
				}

				answer = a1 / a2;
				challenge = a1 + " / " + a2;
				break;

			case TYPE_MATH_MULTIPLY:
				a1 = getRandomInt();
				a2 = getRandomInt();
				answer = a1 * a2;
				challenge = a1 + " * " + a2;
				break;

			case TYPE_MATH_MODULO:
				a1 = getRandomInt();
				a2 = getRandomInt();
				
				if(a2 == 0)
				{
					nextChallenge();
					return;
				}

				answer = a1 % a2;
				challenge = a1 + " % " + a2;
				break;
				
			case TYPE_LOGIC_AND:
				a1 = getRandomInt();
				a2 = getRandomInt();
				answer = a1 & a2;
				challenge = Integer.toBinaryString(a1) + " & " + Integer.toBinaryString(a2);
				break;

			case TYPE_LOGIC_OR:
				a1 = getRandomInt();
				a2 = getRandomInt();
				answer = a1 | a2;
				challenge = Integer.toBinaryString(a1) + " | " + Integer.toBinaryString(a2);
				break;

			case TYPE_LOGIC_XOR:
				a1 = getRandomInt();
				a2 = getRandomInt();
				answer = a1 ^ a2;
				challenge = Integer.toBinaryString(a1) + " ^ " + Integer.toBinaryString(a2);
				break;

			case TYPE_CONVERT_HEX:
				answer = getRandomInt();
				challenge = "0x" + Integer.toHexString(answer).toUpperCase();
				break;

			case TYPE_CONVERT_OCTAL:
				answer = getRandomInt();
				challenge = "0" + Integer.toOctalString(answer);
				break;
				
			default:
			case TYPE_CONVERT_BINARY:
				answer = getRandomInt();
				challenge = Integer.toBinaryString(answer);
				break;
		}
		
		setChallenge(challenge);
		setAnswer(answer);
		
		txtAnswer.setText("");
		txtAnswer.requestFocusInWindow();
	}
	
	protected void solveChallenge()
	{
		String answerExpr = txtAnswer.getText();
		
		// try to evaluate answer expr
		/*
	    try
		{
	    	if(answerExpr != null && !answerExpr.isEmpty())
	    	{
				ScriptEngineManager mgr = new ScriptEngineManager();
			    ScriptEngine engine = mgr.getEngineByName("JavaScript");
			    
				answerExpr = engine.eval(answerExpr).toString();	    		
	    	}
		}
		catch (ScriptException e1)
		{
		}
		*/
		
		// check answer
		int correct = 0;
		int answer;
		
		try
		{
			answer = (int) Float.parseFloat(answerExpr);

			if(answer == this.answer)
			{
				txtAnswer.setBackground(Color.GREEN);
				
				correct = 1;
			}
			else
			{
				txtAnswer.setBackground(Color.RED);
				
				correct = 0;
			}
		}
		catch(NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// insert answer
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Statement stmt = connection.createStatement();
			String sql = "INSERT INTO results (`challenge`, `answer`, `correct`, `date`) ";
			sql+= "VALUES ('" + challenge + "', " + answer + ", " + correct + ", '" + sdf.format(new Date()) + "')";

			stmt.executeUpdate(sql);
		}
		catch(SQLException e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if(correct == 1)
		{
			nextChallenge();
		}
	}

	protected void answerChallenge()
	{
		txtAnswer.setText("" + this.answer);
	}
	
	protected void setChallenge(String challenge)
	{
		this.challenge = challenge;
		
		lblChallenge.setText(challenge);
	}
	
	protected void setAnswer(int answer)
	{
		this.answer = answer;
	}
	
	protected int getRandomInt(int max)
	{
		return (int) (Math.random() * max);
	}

	protected int getRandomInt()
	{
		return getRandomInt(getMaxInt());
	}

	protected int getMaxInt()
	{
		return maxInteger;
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
	
	protected void refreshSettings()
	{
		Iterator<Entry<Integer, JCheckBox>> it = settingTypes.entrySet().iterator();
		types.clear();
		
		while(it.hasNext())
		{
			Entry<Integer, JCheckBox> entry = it.next();
			
			if(entry.getValue().isSelected())
			{
				types.add(entry.getKey());
			}
		}
		
		if(types.size() == 0)
		{
			types.add(TYPE_CONVERT_BINARY);
		}
	}
	
	protected Component buildChallengePanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		
		// task
		lblChallenge = new JLabel("-");
		lblChallenge.setFont(new Font("Arial", Font.BOLD, 24));
		lblChallenge.setBorder(new EmptyBorder(8, 8, 8, 8));
		
		panel.add(lblChallenge, BorderLayout.NORTH);
		
		// answer
		txtAnswer = new JTextField();
		txtAnswer.setFont(new Font("Arial", Font.BOLD, 24));
		txtAnswer.setBorder(new EmptyBorder(8, 8, 8, 8));
		txtAnswer.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					solveChallenge();
				}
			}

			public void keyReleased(KeyEvent e)
			{
			}

			public void keyTyped(KeyEvent e)
			{
			}
			
		});
		
		panel.add(txtAnswer, BorderLayout.CENTER);
		
		// buttons
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		btnSolve = new JButton("Solve");
		btnSolve.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				solveChallenge();
			}
			
		});
		
		btnSkip = new JButton("Skip");
		btnSkip.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				nextChallenge();
				
				// reset bg color
				txtAnswer.setBackground(new JTextField().getBackground());
			}
			
		});
		
		btnAnswer = new JButton("Answer");
		btnAnswer.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				answerChallenge();
			}
			
		});
		
		panelButtons.add(btnSolve);
		panelButtons.add(btnSkip);
		panelButtons.add(btnAnswer);
		
		panel.add(panelButtons, BorderLayout.SOUTH);
		
		return panel;
	}
		
	protected Component buildSettingPanel()
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setPreferredSize(new Dimension(300, 600));
		
		JPanel panelDifficulty = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblDifficulty = new JLabel("Difficulty");
		lblDifficulty.setPreferredSize(new Dimension(100, 24));

		JSlider sldDifficulty = new JSlider(JSlider.HORIZONTAL, 8, 128, maxInteger);
		sldDifficulty.setPreferredSize(new Dimension(200, 50));
		sldDifficulty.setMajorTickSpacing(20);
		sldDifficulty.setPaintTicks(true);
		sldDifficulty.setPaintLabels(true);
		sldDifficulty.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();

			    if(!source.getValueIsAdjusting())
			    {
			    	maxInteger = (int) source.getValue();
			    }
			}

		});

		panelDifficulty.add(lblDifficulty);
		panelDifficulty.add(sldDifficulty);
		
		panel.add(panelDifficulty);
		
		settingTypes.put(TYPE_CONVERT_BINARY, new JCheckBox("Convert Binary"));
		settingTypes.put(TYPE_CONVERT_OCTAL, new JCheckBox("Convert Octal"));
		settingTypes.put(TYPE_CONVERT_HEX, new JCheckBox("Convert Hex"));
		settingTypes.put(TYPE_MATH_ADDITION, new JCheckBox("Math Addition"));
		settingTypes.put(TYPE_MATH_SUBTRACT, new JCheckBox("Math Subtract"));
		settingTypes.put(TYPE_MATH_DIVIDE, new JCheckBox("Math Divide"));
		settingTypes.put(TYPE_MATH_MULTIPLY, new JCheckBox("Math Multiply"));
		settingTypes.put(TYPE_MATH_MODULO, new JCheckBox("Math Modulo"));
		settingTypes.put(TYPE_LOGIC_AND, new JCheckBox("Logic AND"));
		settingTypes.put(TYPE_LOGIC_OR, new JCheckBox("Logic OR"));
		settingTypes.put(TYPE_LOGIC_XOR, new JCheckBox("Logic XOR"));

		Iterator<Entry<Integer, JCheckBox>> it = settingTypes.entrySet().iterator();

		while(it.hasNext())
		{
			Entry<Integer, JCheckBox> entry = it.next();
			JCheckBox ckbType = entry.getValue();
			
			JPanel panelType = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			JLabel lblType = new JLabel(ckbType.getText());
			lblType.setPreferredSize(new Dimension(100, 24));

			ckbType.setText("");
			ckbType.setPreferredSize(new Dimension(200, 30));
			ckbType.setSelected(true);
			ckbType.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e)
				{
					refreshSettings();
				}
				
			});

			panelType.add(lblType);
			panelType.add(ckbType);
			
			panel.add(panelType);
		}
		
		refreshSettings();
		
		return new JScrollPane(panel);
	}
}
