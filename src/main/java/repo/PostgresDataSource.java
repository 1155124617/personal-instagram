package repo;

import classes.Image;
import classes.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@Configuration
@Repository
public class PostgresDataSource {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Connection connection;

    public void createUserTable() throws Exception {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user_info (" +
                "username varchar(20) primary key," +
                "password varchar(20) not null," +
                "createtime timestamp not null)");
        stmt.close();
    }

    public void createImageTable() throws Exception {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS images (" +
                "filename varchar(50) primary key," +
                "owner varchar(20) not null," +
                "createtime timestamp not null)");
        stmt.close();
    }

    public void save(User user) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO user_info VALUES (?, ?, ?)");
        stmt.setString(1, user.getName());
        stmt.setString(2, user.getPassword());
        stmt.setTimestamp(3, user.getTimestamp());
        stmt.executeUpdate();
        stmt.close();
    }

    public void save(Image image) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO images VALUES (?, ?, ?)");
        stmt.setString(1, image.getFileName());
        stmt.setString(2, image.getOwner());
        stmt.setTimestamp(3, image.getUploadTime());
        stmt.executeUpdate();
        stmt.close();
    }

    // The username is the key of a user_info record
    public User findUserByName(String userName) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user_info WHERE username=?");
        stmt.setString(1, userName);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new User(rs.getString("username"),
                            rs.getString("password"),
                            rs.getTimestamp("createtime"));
        }
        else {
            return null;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM user_info");

        List<User> users = new LinkedList<>();
        while(rs.next()) {
            users.add(new User(rs.getString("username"),
                               rs.getString("password"),
                               rs.getTimestamp("createtime")));
        }
        return users;
    }

    public List<Image> getImages() throws SQLException{  // Return 9 images by default
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM images LIMIT ?");
        stmt.setInt(1, 9);
        ResultSet rs = stmt.executeQuery();

        List<Image> images = new LinkedList<>();
        while (rs.next()) {
            images.add(new Image(rs.getString("filename"),
                                 rs.getString("owner"),
                                 rs.getTimestamp("createtime")));
        }
        return images;
    }

    public List<Image> getImages(int num) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM images LIMIT ?");
        stmt.setInt(1, num);
        ResultSet rs = stmt.executeQuery();

        List<Image> images = new LinkedList<>();
        if (rs.next()) {
            images.add(new Image(rs.getString("filename"),
                                 rs.getString("owner"),
                                 rs.getTimestamp("createtime")));
        }
        return images;
    }

    public List<Image> getAllImages() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM images");

        List<Image> images = new LinkedList<>();
        while (rs.next()) {
            images.add(new Image(rs.getString("filename"),
                                 rs.getString("owner"),
                                 rs.getTimestamp("createtime")));
        }
        return images;
    }

    public void deleteUserByName(String userName) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM user_info WHERE username=?");
        stmt.setString(1, userName);
        stmt.executeUpdate();
        stmt.close();
    }

    public void deleteImageByName(String fileName) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM images WHERE filename=?");
        stmt.setString(1, fileName);
        stmt.executeUpdate();
        stmt.close();
    }

    public void deleteImageByOwner(String owner) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM images WHERE owner=?");
        stmt.setString(1, owner);
        stmt.executeUpdate();
        stmt.close();
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        }
        else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            return new HikariDataSource(config);
        }
    }

    @Bean
    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }
}
