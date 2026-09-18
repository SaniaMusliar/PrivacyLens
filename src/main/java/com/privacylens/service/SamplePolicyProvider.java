package com.privacylens.service;

import org.springframework.stereotype.Service;

/**
 * Provides a built-in sample privacy policy so the application can be
 * demonstrated end-to-end without requiring a file upload.
 */
@Service
public class SamplePolicyProvider {

    public String getSamplePolicyText() {
        return """
                Sample Company Privacy Policy

                Last updated: January 2026

                This privacy policy explains how Sample Company collects, uses, shares, and protects personal information when you use our website and mobile application.

                Information We Collect

                When you create an account, we collect your name, email address, and password. If you make a purchase, we also collect your payment information and billing address. When you use our mobile application, we automatically collect device information such as your device type, operating system, and IP address. We also collect location information if you enable location services, and we use cookies to remember your preferences and track usage of our website.

                Why We Collect Information

                We collect this information in order to provide and improve our services, to process your transactions, to communicate with you about your account, and to personalize your experience. We also use certain information to maintain the security of our platform and to comply with legal obligations.

                How We Share Your Information

                We may share your personal information with third-party service providers who help us operate our business, such as payment processors and cloud hosting providers. We do not sell your personal information to third parties. We may share information with advertising partners to show you personalized advertisements based on your interests and browsing behavior. We may also disclose information if required by law or to protect the rights and safety of our users.

                Data Retention

                We retain your account information for as long as your account remains active. If you delete your account, we will delete your personal information within 90 days, except where we are required to retain certain records for legal or regulatory purposes.

                Your Rights and Choices

                You can access and update your account information at any time through your account settings. You may request deletion of your personal information by contacting our support team. You can opt out of receiving marketing emails by clicking the unsubscribe link in any promotional email. You can disable cookies through your browser settings, though this may affect some features of our website.

                Advertising

                We work with third-party advertising partners to display personalized ads. These partners may use cookies and similar technologies to collect information about your browsing activity across different websites in order to show you relevant advertisements.

                Children's Privacy

                Our services are not directed to children under 13, and we do not knowingly collect personal information from children under 13.

                Changes to This Policy

                We may update this privacy policy from time to time. We will notify you of any material changes by posting the new policy on this page.

                Contact Us

                If you have any questions about this privacy policy, please contact our support team through the contact page on our website.
                """;
    }
}
