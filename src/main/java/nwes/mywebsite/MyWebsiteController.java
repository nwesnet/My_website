package nwes.mywebsite;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class MyWebsiteController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/projects/passwordmanager")
    public String passwordManager() {
        return "Projects/Passwordmanager";
    }

    @GetMapping("/projects/dictionary")
    public String dictionary() {
        return "Projects/Dictionary";
    }

    @GetMapping("/patches")
    public String patches() {
        return "patches"; // create patches.html
    }
    @GetMapping("/preferences")
    public String preferences() {
        return "preferences";
    }

    @GetMapping("/app/passwordmanagerapp")
    public String passwordmanagerapp() {
        return "app/passwordmanagerapp";
    }
    @GetMapping("/app/dictionaryapp")
    public String dictionaryapp() {
        return "app/dictionaryapp";
    }
}

