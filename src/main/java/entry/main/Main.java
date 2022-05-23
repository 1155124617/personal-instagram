package entry.main;

import classes.Image;
import classes.ImageBlob;
import classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import repo.PostgresDataSource;
import utils.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@SpringBootApplication(scanBasePackages = {"entry.main", "classes", "repo", "conf"})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Autowired
    private PostgresDataSource postgresDataSource;

    public void createIfNotExists(Image image) throws SQLException, IOException {
        String dir = "user-images/" + image.getOwner();

        if (!FileUtil.exists(dir + "/" + image.getFileName())) {
            ImageBlob imageBlob = postgresDataSource.getImageBlobByName(image.getFileName());
            FileUtil.saveFile(dir, imageBlob.getImageName(), imageBlob.getContent());
        }
    }

    public void createIfNotExists(List<Image> images) throws SQLException, IOException {
        for (Image image : images) {
            createIfNotExists(image);
        }
    }

    @RequestMapping(value="/", method=RequestMethod.GET)
    String index(HttpServletRequest request, Map<String, Object> model, HttpSession session) {
        // Get and Display IP Address and Host Name
        String ipAddress = request.getRemoteAddr();
        String hostName = request.getRemoteHost();

        model.put("ip_address", ipAddress);
        model.put("host_name", hostName);

        String userName = (String)session.getAttribute("user");
        if (userName != null) {
            model.put("user_logged_in", true);
            model.put("username", userName);
        }

        try {
            List<Image> images = postgresDataSource.getAllImages();
            createIfNotExists(images);
            List<String> imagePaths = new LinkedList<>();

            for (Image image : images) {
                imagePaths.add("user-images/" + image.getOwner() + "/" + image.getFileName());
            }

            List<List<String>> imageGroups = new LinkedList<>();
            for (int i = 0 ; i < imagePaths.size() ; i+=3) {
                imageGroups.add(imagePaths.subList(i, Math.min(i+3, imagePaths.size())));
            }

            model.put("num_images", images.size());
            model.put("image_groups", imageGroups);
        } catch (Exception e) {
            model.put("message", e);
            System.out.println("Failed to get images from the database");
            return "error";
        }

        return "index";
    }

    @RequestMapping(value="/user/signup", method = RequestMethod.GET)
    String signup() {
        return "signup";
    }

    @RequestMapping(value="/user/signup", method=RequestMethod.POST)
    String signup(WebRequest request, Map<String, Object> model) {
        String userName = request.getParameter("signup-user-name");
        String password = request.getParameter("signup-password");
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

        User user = new User(userName, password, timeStamp);

        System.out.println(user);

        // Add user information to the database table
        try {
            postgresDataSource.save(user);
            System.out.println("Insert successfully");
            return "redirect:/";
        } catch (Exception e) {
            model.put("message", e);
            System.out.println("Failed to insert");
            return "error";
        }
    }

    @RequestMapping(value="/user/signin", method = RequestMethod.GET)
    String signin() {
        return "signin";
    }

    @RequestMapping(value="/user/signin", method=RequestMethod.POST)
    String signin(WebRequest request, Map<String, Object> model, HttpSession session) {
        String userName = request.getParameter("signin-user-name");
        String password = request.getParameter("signin-password");

        try {
            User user = postgresDataSource.findUserByName(userName);
            if (user != null) {
                if (user.getPassword().equals(password)) {
                    session.setAttribute("user", user.getName());
                    return "redirect:/";
                } else {
                    model.put("message", "Wrong password");
                    System.out.println("Wrong password");
                    return "error";
                }
            } else {
                model.put("message", "The user is not found");
                System.out.println("The user is not found");
                return "error";
            }
        } catch (SQLException e) {
            model.put("message", e);
            System.out.println("Failed when find the user in the database");
            return "error";
        }
    }

    @RequestMapping(value="/user/logout", method=RequestMethod.GET)
    String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

    @RequestMapping(value="/user/upload", method=RequestMethod.POST)
    String uploadImage(@RequestParam("upload-image") MultipartFile multipartFile, Map<String, Object> model,
                       HttpSession session) {
        if (multipartFile.isEmpty()) {
            System.out.println("Empty MultipartFile");
            model.put("message", "Please select image");
            return "error";
        }
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        String owner = (String)session.getAttribute("user");
        if (owner == null) {
            model.put("message", "You have not signed in");
            System.out.println("The user has not signed in");
            return "error";
        }

        Image image = new Image(multipartFile, fileName, owner, new Timestamp(System.currentTimeMillis()));

        String uploadDir = "user-images/" + image.getOwner();
        try {
            FileUtil.saveFile(uploadDir, image.getFileName(), image.getMultipartFile());
            postgresDataSource.save(image);
            postgresDataSource.save(new ImageBlob(image.getFileName(), multipartFile.getBytes(), owner));
            System.out.println("Upload image successfully");
            return "redirect:/";
        } catch (IOException e) {
            System.out.println("Failed to upload image");
            model.put("message", e);
            return "error";
        } catch (Exception e) {
            System.out.println("Failed to insert into the database");
            model.put("message", e);
            return "error";
        }
    }

    @RequestMapping("admin")
    String adminPage(Map<String, Object> model) {
        try {
            List<User> users = postgresDataSource.getAllUsers();
            List<Image> images = postgresDataSource.getAllImages();

            createIfNotExists(images);
            List<String> imagePaths = new LinkedList<>();
            for (Image image : images) {
                imagePaths.add("user-images/" + image.getOwner() + "/" + image.getFileName());
            }

            model.put("users", users);
            model.put("image_paths", imagePaths);
            model.put("images", images);

            return "admin";
        } catch (Exception e) {
            System.out.println("Failed to get all users");
            model.put("message", "Failed to get all users");
            return "error";
        }
    }

    @RequestMapping("/admin/create_tables")
    String createTables(Map<String, Object> model) {
        // Create database table
        try {
            postgresDataSource.createUserTable();
            postgresDataSource.createImageTable();
            postgresDataSource.createImageBlobTable();
            System.out.println("Create successfully");
            return "redirect:/";
        } catch (Exception e) {
            model.put("message", e);
            System.out.println("Failed to create");
            return "error";
        }
    }

    @RequestMapping(value = "admin/delete_user", method = RequestMethod.POST)
    String deleteUser(@RequestParam("delete-username") String userName, Map<String, Object> model) {
        String deleteDir = "user-images/" + userName;

        try {
            FileUtil.deleteDirectory(deleteDir);
            postgresDataSource.deleteUserByName(userName);
            postgresDataSource.deleteImageByOwner(userName);
            postgresDataSource.deleteImageBlobByOwner(userName);
            return "redirect:/admin";
        } catch (Exception e) {
            System.out.println("Failed to delete directory " + deleteDir);
            model.put("message", e);
            return "error";
        }
    }

    @RequestMapping(value="admin/delete_image", method = RequestMethod.POST)
    String deleteImage(@RequestParam("delete-filename") String fileName,
                       @RequestParam("delete-owner") String owner,
                       Map<String, Object> model) {
        String deleteDir = "user-images/" + owner + "/" + fileName;
        try {
            FileUtil.deleteFile(deleteDir);
            postgresDataSource.deleteImageByName(fileName);
            postgresDataSource.deleteImageBlobByName(fileName);
            return "redirect:/admin";
        } catch (Exception e) {
            System.out.println("Failed to delete " + fileName + " in database");
            model.put("message", "Failed to delete " + fileName + " in database");
            return "error";
        }
    }

    @RequestMapping("/admin/drop_tables")
    String dropTables(Map<String, Object> model) {
        try {
            postgresDataSource.dropUserTable();
            postgresDataSource.dropImageTable();
            postgresDataSource.dropImageBlobTable();

            return "redirect:/";
        } catch (SQLException e) {
            System.out.println("Failed to drop tables");
            model.put("message", "Failed to drop tables");
            return "errpr";
        }
    }
}
