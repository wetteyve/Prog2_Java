package dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;

import javax.sql.DataSource;

import model.Picture;
import dao.PictureDAO;

public class PictureJdbcDAO implements PictureDAO {
    private DataSource dataSource;

    public PictureJdbcDAO(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Picture item) {
        // TODO Implement method
        if (item == null) {
            throw new IllegalArgumentException("null is not allowed.");
        }
        if (item.getId() != -1) {
            throw new IllegalArgumentException("Picture is already inserted");
        }
        try (Connection conn = dataSource.getConnection()) {
            String queue = "INSERT INTO picture "
                + "(date, longitude, latitude, altitude, title, comment, url) "
                + "values (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement st = conn.prepareStatement(queue, Statement.RETURN_GENERATED_KEYS);
            st.setDate(1, new java.sql.Date(item.getDate().getTime()));
            st.setFloat(2, item.getLongitude());
            st.setFloat(3, item.getLatitude());
            st.setFloat(4, item.getAltitude());
            st.setString(5, item.getTitle());
            st.setString(6, item.getComment());
            st.setString(7, item.getUrl().toString());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting picture failed, no rows affected.");
            }
            ResultSet newKeys = st.getGeneratedKeys();
            if (newKeys.next()) {
                item.setId(newKeys.getInt("id"));
            } else {
                throw new SQLException("Inserting picture failed, no generated key obtained.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB operation failed: insert(picture): " + item, e);
        }
    }

    @Override
    public void update(Picture item) {
        // TODO Implement method
        if (item == null) {
            throw new IllegalArgumentException("null is not allowed.");
        }
        if (item.getId() == -1) {
            throw new IllegalArgumentException("Illegal pictureId: picture not available.");
        }
        try (Connection conn = dataSource.getConnection()) {
            String queue = "update picture "
                + "set date=?, longitude=?, latitude=?, altitude=?, title=?, comment=?, url=? "
                + "where id=?";
            PreparedStatement st = conn.prepareStatement(queue);
            st.setDate(1,new java.sql.Date(item.getDate().getTime()));
            st.setFloat(2, item.getLongitude());
            st.setFloat(3, item.getLatitude());
            st.setFloat(4, item.getAltitude());
            st.setString(5, item.getTitle());
            st.setString(6, item.getComment());
            st.setString(7, item.getUrl().toString());
            st.setInt(8, item.getId());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating picture failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB operation failed: update(picture): " + item, e);
        }
    }

    @Override
    public void delete(Picture item) {
        // TODO Implement method
        if (item == null) {
            throw new IllegalArgumentException("null is not allowed.");
        }
        if (item.getId() == -1) {
            throw new IllegalArgumentException("Illegal pictureId: picture not available.");
        }
        try (Connection conn = dataSource.getConnection()) {
            String queue = "delete from picture where id=?";
            PreparedStatement st = conn.prepareStatement(queue);
            st.setInt(1, item.getId());
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting picture failed, no rows affected.");
            }
            item.setId(-1); //set item invalid
        } catch (SQLException e) {
            throw new RuntimeException("DB operation failed: delete(picture): " + item, e);
        }
    }

    @Override
    public int count() {
        // TODO Implement method
        int count = -1;
        try (Connection conn = dataSource.getConnection()){
            String queue = "select count(*) form picture";
            PreparedStatement st = conn.prepareStatement(queue);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                throw new RuntimeException("Could not get db row count!");
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB operation failed: count(): ", e);
        }
        return count;
    }

    @Override
    public Picture findById(int id) {
        // TODO Implement method
        Picture picture = null;
        try (Connection conn = dataSource.getConnection()) {
            String queue = "select id, date, longitude, latitude, altitude, title, comment, url "
                + "from picture where id =  ?";
            PreparedStatement st = conn.prepareStatement(queue);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            List<Picture> pictures = instanciateItems(rs);
            if(pictures.isEmpty()) {
                throw new RuntimeException("Picture with id " + id + "not found!");
            } else {
                picture = pictures.get(0);
            }
        } catch (SQLException | MalformedURLException e) {
            throw new RuntimeException("DB operation failed: print(picture): " + id, e);
        }
        return picture;
    }

    @Override
    public Collection<Picture> findAll() {
        List<Picture> pictures = null;
        // TODO Implement method
        try (Connection conn = dataSource.getConnection()){
            String queue = "select id, date, longitude, latitude, altitude, title, comment, url "
                + "from picture";
            PreparedStatement st = conn.prepareStatement(queue);
            ResultSet rs = st.executeQuery();
            pictures = instanciateItems(rs);
        } catch (SQLException | MalformedURLException e) {
            throw new RuntimeException("DB operation failed: finAll(): ", e);
        }
        return pictures;
    }

    @Override
    public Collection<Picture> findByPosition(float longitude,
            float latitude, float deviation) {
        List<Picture> pictures = null;
        // TODO Implement method
        try (Connection conn = dataSource.getConnection()) {
            String queue = "select id, date, longitude, latitude, altitude, title, comment, url "
                + "from picture where longitude<=? and longitude>=? and latitude<=? and latitude>=?";
            PreparedStatement st = conn.prepareStatement(queue);
            st.setFloat(1, longitude+deviation);
            st.setFloat(2, longitude-deviation);
            st.setFloat(3,latitude+deviation);
            st.setFloat(4,latitude-deviation);
            ResultSet rs = st.executeQuery();
            pictures = instanciateItems(rs);
        } catch (SQLException | MalformedURLException e) {
            throw new RuntimeException("DB operation failed: findByPosition(): ", e);
        }
        return pictures;
    }

    //Logik um ein Bild Objekt zu erstellen, wird in benutzt um ein Bild aus der DB auszugeben
    private List<Picture> instanciateItems(ResultSet rs)
        throws SQLException, MalformedURLException {
        List<Picture> pictures = new ArrayList<Picture>();
        while (rs.next()) {
            Picture picture = new Picture(
                rs.getInt("id"),
                new URL(rs.getString("url")),
                rs.getDate("date"),
                rs.getString("title"),
                rs.getString("comment"),
                rs.getFloat("longitude"),
                rs.getFloat("latitude"),
                rs.getFloat("altitude"));
            pictures.add(picture);
        }
        return pictures;
    }

}
