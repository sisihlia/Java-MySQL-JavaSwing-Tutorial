package tutorial;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainWindow {
    private JTable table;
    private JPanel rootPanel;
    private JTextField nameField;
    private JTextField idField;
    private JTextField quantityField;
    private JPanel date;
    private JPanel image;
    private JTextField priceField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton uploadButton;
    private JLabel imageToAdd;
    private JButton firstButton;
    private JButton lastButton;
    private JButton nextButton;
    private JButton previousButton;


    JDateChooser dateChooser = new JDateChooser();
    String imgPath=null;
    int pos =0;
    public MainWindow() {

        JFrame frame= new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setSize(700, 600);
        date.add(dateChooser);

        showProducts();


        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser file = new JFileChooser();
                file.setCurrentDirectory(new File(System.getProperty("user.home")));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.images", "jpg", "png");
                file.addChoosableFileFilter(filter);
                int result = file.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selected = file.getSelectedFile();
                    String path = selected.getAbsolutePath();
                    imageToAdd.setIcon(resizeImage(path, null));
                    imgPath=path;

                }
            }
        });


        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (checkInputs() && imgPath !=null) {
                    String state="Insert into products (id,name,price,quantity,date,image) values(?,?,?,?,?,?)";
                    getConnectWithStatement(state,Optional.of(getImagePath()));
                    showProducts();
                }else {
                    JOptionPane.showMessageDialog(null,"Fields are not complete");
                }
            }
        });


        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
              if (checkInputs() && idField.getText()!=null) {

                  String update="UPDATE products SET name = ?, price = ?" + ", date = ? WHERE id = ? ";
                  String  updateWithImage="UPDATE products SET name = ?, price = ? ,  date = ?, image= ? WHERE id = ? ";
                  if (getImagePath()!=null ) {
                     getConnectWithStatement(updateWithImage,Optional.of(getImagePath()));
                  }else {
                    getConnectWithStatement(update,null);
                  }
                  showProducts();
             }

            }
        });


        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!idField.getText().isEmpty()){
                    String state="DELETE from products WHERE id = ?";
                    getConnectWithStatement(state,null);
                }else {
                    JOptionPane.showMessageDialog(null,"Failed to delete the product. Enter the right product id");
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               // super.mouseClicked(e);
                int index = table.getSelectedRow();
                showDetail(index);
            }
        });

        firstButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showDetail(0);
            }
        });
        lastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showDetail(getProducts().size()-1);
            }
        });
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pos--;
                if (pos<0) pos=0;
                showDetail(pos);

            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pos++;
                if (pos>=getProducts().size()) pos=getProducts().size()-1;
                showDetail(pos);
            }
        });
    }


    public void showDetail (int index) {
            idField.setText(Integer.toString(getProducts().get(index).getId()));
            nameField.setText(getProducts().get(index).getName());
            priceField.setText(Float.toString(getProducts().get(index).getPrice()));
            quantityField.setText(Float.toString(getProducts().get(index).getQuantity()));
            try {
                dateChooser.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(getProducts().get(index).getDate()));
            } catch (ParseException e) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, e);
            }

            imageToAdd.setIcon(resizeImage(null, getProducts().get(index).getImage()));
    }

    public InputStream getImagePath () {
        InputStream image=null;
        if (imgPath!=null && !imgPath.isEmpty()) {
            try {
                image= new FileInputStream(new File(imgPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return image;
        }else {
            return image;
        }
    }


    public void getConnectWithStatement (String statement, Optional<InputStream> imagePath) {
        try {
            Connection con= getConnection();
            PreparedStatement ps = con.prepareStatement(statement);
            if (statement.contains("UPDATE")) {

                if (statement.contains("image")) {
                    //update with image
                    System.out.println("im in update with image");
                    ps.setBlob(4, imagePath.get());
                    ps.setInt(5, Integer.parseInt(idField.getText()));

                } else {
                    //update without image
                    System.out.println("im in update w/o image");
                    ps.setInt(4, Integer.parseInt(idField.getText()));
                }
                ps.setString(1, nameField.getText());
                ps.setString(2, priceField.getText());
                ps.setString(3, getDateFormat());
              //  ps.executeUpdate();
            }else if (statement.contains("DELETE")) {
                System.out.println("im in delete");
                int id = Integer.parseInt(idField.getText());
                ps.setInt(1,id);
               // ps.executeUpdate();

            }else {
                System.out.println("im in last else");
                ps.setString(1,idField.getText());
                ps.setString(2,nameField.getText());
                ps.setString(3,priceField.getText());
                ps.setString(4,quantityField.getText());
                ps.setString(5,getDateFormat());
                ps.setBlob(6,imagePath.get());

            }
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null,e);
        }

    }

    public String getDateFormat (){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return  sdf.format(dateChooser.getDate());
    }

    //check input exists

    public boolean checkInputs(){
        if (idField.getText()==null || nameField.getText()==null || quantityField.getText()==null || dateChooser.getDate()==null ) return false;
        else {
            try {
                Float.parseFloat(priceField.getText());
                return true;
            }catch (Exception e) {
                return false;
            }
        }
    }


    public Connection getConnection() {
        Connection con=null;
        try {
            con= DriverManager.getConnection("jdbc:mysql://localhost/patukangan_jaya?useTimezone=true&serverTimezone=UTC","root","root");
          //  JOptionPane.showMessageDialog(null,"Connected");
            return con;
        } catch (SQLException e) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null,e);
            JOptionPane.showMessageDialog(null,"Not Connected");
            return null;
        }
    }





    public ImageIcon resizeImage (String imagePath, byte [] pic) {
        ImageIcon myImage = null;
        if (imagePath!=null) {
            myImage= new ImageIcon((imagePath));
        }else {
            myImage= new ImageIcon(pic);
        }

        Image img = myImage.getImage().getScaledInstance(150,150,Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }


    public static void main (String [] args) {
        new MainWindow();
    }

    public  void showProducts () {
       // createTable();
        ArrayList<Product> list = getProducts();
        table.setModel(new DefaultTableModel(
                null,
                new String [] {"ID","Name","Price","Quantity", "Date"}
        ));

        list.stream().forEach(i -> System.out.println(i.getId() + " " + i.getName() + " " + i.getPrice() + " " + i.getQuantity() + " "+ i.getDate()));
        Object [] row = new Object[5];
        DefaultTableModel model =(DefaultTableModel) table.getModel();
        for (int i =0; i<list.size(); i++) {
            row [0]=list.get(i).getId();
            row [1]=list.get(i).getName();
            row [2]=list.get(i).getPrice();
            row [3]=list.get(i).getQuantity();
            row [4]=list.get(i).getDate();
            model.addRow(row);
        }
    }



    public ArrayList<Product> getProducts (){
        ArrayList<Product> products = new ArrayList<>();
        Connection con=getConnection();
        String query="SELECT * FROM products";
        Statement st;
        ResultSet rs;
        try {
            st=con.createStatement();
            rs=st.executeQuery(query);
            Product product;
            while (rs.next()) {
                product= new Product(rs.getInt("id"),rs.getString("name"),Float.parseFloat(rs.getString("price")),rs.getString("date"),Float.parseFloat(rs.getString("quantity")),rs.getBytes("image"));
                products.add(product);
            }
        }catch (SQLException ex) {

        }
        return  products;
    }

}

class Product {

    private int id;
    private String name;
    private float price;
    private String date;
    private float quantity;
    private byte [] image;


    public Product(int id, String name, float price, String date, float quantity, byte[] image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.date = date;
        this.quantity = quantity;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}