package org.tbk.spring.lnurl.security.ui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.HtmlUtils;
import org.tbk.spring.lnurl.security.ui.LoginScriptGenerator.ScriptConfig;

import static java.util.Objects.requireNonNull;

@Slf4j
final class LoginPageGenerator {

    private final String script;
    private final String stylesheet;
    private final String htmlTemplate;

    LoginPageGenerator(ScriptConfig scriptConfig) {
        requireNonNull(scriptConfig);

        this.script = new LoginScriptGenerator(scriptConfig).createScript();

        this.stylesheet = ""
                + StaticTemplateUtils.readContents("normalize.css")
                + "\n"
                + StaticTemplateUtils.readContents("login.css")
                + "\n";

        this.htmlTemplate = StaticTemplateUtils.readContents("login.html");
    }

    public String createScript() {
        return script;
    }

    public String createStylesheet() {
        return stylesheet;
    }

    public String createLoginPage(String stylesheetUrl, String scriptUrl, LnurlQrcode lnurlQrcode) {
        return this.htmlTemplate
                .replace("%%_LNURL_AUTH_LOGIN_CSS_HREF_%%", HtmlUtils.htmlEscape(stylesheetUrl))
                .replace("%%_LNURL_AUTH_LOGIN_SCRIPT_SRC_%%", HtmlUtils.htmlEscape(scriptUrl))
                .replace("%%_LNURL_AUTH_LOGIN_QR_CODE_SECTION_%%", lnurlQrcode.createHtmlImageTagOrFallbackTag())
                .replace("%%_LNURL_AUTH_LOGIN_LNURL_STRING_%%", lnurlQrcode.toLnurlString())
                ;
    }
}