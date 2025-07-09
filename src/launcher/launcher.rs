use dioxus::prelude::*;
use dioxus_desktop::{Config, WindowBuilder, LogicalSize};

pub fn main() {
    let size = LogicalSize::new(1280.0, 832.0);
    // let size_dev_test = LogicalSize::new(768.0, 400.0);

    let config = Config::default().with_window(
        WindowBuilder::new()
            .with_title("Dream Launcher")
            .with_min_inner_size(size)
            .with_max_inner_size(size)
            .with_resizable(true)
    );

    LaunchBuilder::new()
        .with_cfg(config)
        .launch(app);
}

fn app() -> Element {
    const LOGO: Asset = asset!("/public//assets/images/logo.png");
    const MICROSOFT: Asset = asset!("/public//assets/images/microsoft.png");
    const FONT_MEDIUM: Asset = asset!("/public//assets/fonts/Gilroy-Medium.ttf");
    const FONT_BOLD: Asset = asset!("/public//assets/fonts/Gilroy-BOLD.ttf");
    rsx! {
        style { "
            @font-face {{
                font-family: 'Gilroy';
                src: url('{FONT_MEDIUM}') format('ttf');
                font-weight: normal;
                font-style: normal;
            }}

            @font-face {{
                font-family: 'Gilroy';
                src: url('{FONT_BOLD}') format('ttf');
                font-weight: 700;
                font-style: normal;
            }}

            * {{
                -webkit-font-smoothing: antialiased;
                box-sizing: border-box;
                -webkit-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
            }}

            img {{
                -webkit-user-drag: none;
                -khtml-user-drag: none;
                -moz-user-drag: none;
                -o-user-drag: none;
                user-drag: none;
                pointer-events: none;
            }}

            html, body {{
                margin: 0px;
                height: 100%;
                overflow: hidden;
            }}

            button:focus-visible {{
                outline: 2px solid #4a90e2 !important;
                outline: -webkit-focus-ring-color auto 5px !important;
            }}

            a {{
                text-decoration: none;
            }}

            body {{
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                margin: 0;
                background-color: #141414;
            }}

            .desktop {{
                background-color: #141414;
                display: flex;
                flex-direction: row;
                justify-content: center;
                width: 100%;
                height: 100vh;
                max-width: 1280px;
            }}

            .content {{
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                padding: 2rem;
                width: 100%;
                height: 100%;
            }}

            .logo {{
                width: 192px;
                height: 120px;
                object-fit: cover;
                margin-bottom: 2rem;
            }}

            .welcome-text {{
                font-family: 'Gilroy', sans-serif;
                font-weight: 700;
                color: #ffffff;
                font-size: 36px;
                text-align: center;
                margin-bottom: 2rem;
            }}

            .login-options {{
                display: flex;
                flex-direction: column;
                gap: 1rem;
                width: 100%;
                max-width: 290px;
            }}

            .login-button {{
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100%;
                height: 55px;
                border-radius: 8px;
                font-family: 'Gilroy', sans-serif;
                font-weight: 500;
                font-size: 18px;
                cursor: pointer;
                border: none;
                transition: background-color 0.3s ease;
            }}

            .microsoft-login {{
                background-color: #ffffff;
                color: #000000;
            }}

            .microsoft-login:hover {{
                background-color: #f0f0f0;
            }}

            .offline-login {{
                background-color: #1b1b1b;
                color: #787878;
            }}

            .offline-login:hover {{
                background-color: #2a2a2a;
            }}

            .microsoft-icon {{
                width: 32px;
                height: 32px;
                margin-right: 1rem;
            }}
        " }

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