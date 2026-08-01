package com.example.elhabashyback.mail;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailTemplateService {

    public String activation(String firstName, String activationUrl) {
        String safeName = HtmlUtils.htmlEscape(firstName);
        String safeUrl = HtmlUtils.htmlEscape(activationUrl);
        String content = """
                <p style="margin:0 0 12px;color:#475569;font-size:15px;line-height:1.9">أهلًا <strong style="color:#0f172a">{{NAME}}</strong>،</p>
                <p style="margin:0 0 24px;color:#475569;font-size:15px;line-height:1.9">شكرًا لإنشاء حسابك على منصة الحبشي. اضغط الزر التالي لتأكيد بريدك الإلكتروني وتفعيل الحساب.</p>
                <a href="{{URL}}" style="display:inline-block;background:#fbbf24;color:#0f172a;text-decoration:none;font-weight:900;font-size:15px;padding:14px 30px;border-radius:999px">تفعيل الحساب</a>
                <p style="margin:26px 0 8px;color:#64748b;font-size:13px;line-height:1.8">الرابط صالح لمدة 24 ساعة. إذا لم تطلب إنشاء الحساب، تجاهل الرسالة.</p>
                <p style="margin:0;color:#94a3b8;font-size:11px;line-height:1.7;word-break:break-all">{{URL}}</p>
                """
                .replace("{{NAME}}", safeName)
                .replace("{{URL}}", safeUrl);
        return layout("تفعيل حسابك", "خطوة واحدة لتفعيل حساب الحبشي", content);
    }

    public String passwordReset(String firstName, String otp) {
        String safeName = HtmlUtils.htmlEscape(firstName);
        String content = """
                <p style="margin:0 0 12px;color:#475569;font-size:15px;line-height:1.9">أهلًا <strong style="color:#0f172a">{{NAME}}</strong>،</p>
                <p style="margin:0 0 20px;color:#475569;font-size:15px;line-height:1.9">استخدم رمز التحقق التالي لإعادة تعيين كلمة المرور:</p>
                <div dir="ltr" style="display:inline-block;background:#0f172a;color:#fbbf24;font-size:32px;font-weight:900;letter-spacing:10px;padding:18px 24px;border-radius:18px">{{OTP}}</div>
                <p style="margin:24px 0 0;color:#64748b;font-size:13px;line-height:1.8">الرمز صالح لمدة 10 دقائق ولمدة 5 محاولات فقط. لا تشاركه مع أي شخص.</p>
                <p style="margin:8px 0 0;color:#94a3b8;font-size:12px;line-height:1.8">إذا لم تطلب تغيير كلمة المرور، يمكنك تجاهل الرسالة بأمان.</p>
                """
                .replace("{{NAME}}", safeName)
                .replace("{{OTP}}", HtmlUtils.htmlEscape(otp));
        return layout("استعادة كلمة المرور", "رمز استعادة حساب الحبشي", content);
    }

    private String layout(String title, String preheader, String content) {
        return """
                <!doctype html>
                <html lang="ar" dir="rtl">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;background:#f1f5f9;font-family:Tahoma,Arial,sans-serif">
                  <div style="display:none;max-height:0;overflow:hidden;color:transparent">{{PREHEADER}}</div>
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f1f5f9;padding:28px 12px">
                    <tr><td align="center">
                      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#ffffff;border-radius:28px;overflow:hidden;box-shadow:0 18px 50px rgba(15,23,42,.10)">
                        <tr><td style="background:#020617;padding:28px 32px;text-align:right">
                          <div style="color:#fbbf24;font-size:13px;font-weight:900;letter-spacing:1px">EL HABASHY</div>
                          <div style="color:#ffffff;font-size:22px;font-weight:900;margin-top:5px">الحبشي للمزادات والتثمين</div>
                        </td></tr>
                        <tr><td style="padding:34px 32px;text-align:right">
                          <div style="display:inline-block;background:#fef3c7;color:#92400e;font-size:12px;font-weight:900;padding:7px 12px;border-radius:999px">حسابك بأمان</div>
                          <h1 style="margin:16px 0 22px;color:#020617;font-size:28px;line-height:1.4">{{TITLE}}</h1>
                          {{CONTENT}}
                        </td></tr>
                        <tr><td style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:20px 32px;text-align:center;color:#64748b;font-size:12px;line-height:1.8">
                          شركة الحبشي للمزادات والتثمين<br>22 شارع محمود بسيوني، قصر النيل، القاهرة
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .replace("{{PREHEADER}}", HtmlUtils.htmlEscape(preheader))
                .replace("{{TITLE}}", HtmlUtils.htmlEscape(title))
                .replace("{{CONTENT}}", content);
    }
}
