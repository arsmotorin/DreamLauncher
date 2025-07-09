use dioxus::prelude::*;
use dioxus_desktop::{Config, WindowBuilder, LogicalSize};

pub(crate) fn launcher() -> Element {
    const LOGO: Asset = asset!("/public/assets/images/logo.png");
    const MICROSOFT: Asset = asset!("/public/assets/images/microsoft.png");

    rsx! {
        style {
            dangerous_inner_html: include_str!("/Users/cubelius/RustroverProjects/Launcher/DreamLauncher/public/assets/styles/style.css")
        }
        main {
            class: "desktop",
            div {
                class: "content",
                img {
                    class: "logo",
                    src: "{LOGO}",
                    alt: "Dream Launcher Logo"
                }
                h1 {
                    class: "welcome-text",
                    "Welcome to Dream Launcher!"
                }
                div {
                    class: "login-options",
                    button {
                        class: "login-button microsoft-login",
                        img {
                            class: "microsoft-icon",
                            src: "{MICROSOFT}",
                            alt: "Microsoft Logo"
                        }
                        span {
                            class: "microsoft-login-text",
                            "Login with Microsoft"
                        }
                    }
                    button {
                        class: "login-button offline-login",
                        span {
                            "Offline account"
                        }
                    }
                }
            }
        }
    }
}