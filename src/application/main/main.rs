use dioxus::prelude::*;

#[component]
pub fn Main() -> Element {
    const LOGO: Asset = asset!("/public/assets/images/other/logo.png");
    const HOME: Asset = asset!("/public/assets/images/buttons/home.png");
    const MODS_AND_PACKS: Asset = asset!("/public/assets/images/buttons/mods_and_packs.png");
    const SETTINGS: Asset = asset!("/public/assets/images/buttons/settings.png");
    const CLOUD: Asset = asset!("/public/assets/images/buttons/cloud.png");
    const PLUS: Asset = asset!("/public/assets/images/buttons/plus.png");

    rsx! {
        // CSS styles
        style {
            dangerous_inner_html: include_str!("/Users/cubelius/RustroverProjects/Launcher/DreamLauncher/public/assets/styles/style_main.css")
        }
        div { class: "desktop",
            nav { class: "navigation",
                div { class: "logo-wrapper",
                    div { class: "logo",
                        img { src: "{LOGO}", alt: "Logo", class: "logo-img" }
                    }
                    h1 { class: "app-name", "Dream Launcher" }
                }
                ul { class: "nav-items",
                    li { class: "nav-item active",
                        img { class: "nav-icon", src: "{HOME}", alt: "Home" }
                        span { class: "nav-text", "Home" }
                    }
                    li { class: "nav-item",
                        img { class: "nav-icon", src: "{MODS_AND_PACKS}", alt: "Mods & Packs" }
                        span { class: "nav-text", "Mods & Packs" }
                    }
                    li { class: "nav-item",
                        img { class: "nav-icon", src: "{SETTINGS}", alt: "Settings" }
                        span { class: "nav-text", "Settings" }
                    }
                    li { class: "nav-item",
                        img { class: "nav-icon", src: "{CLOUD}", alt: "Cloud" }
                        span { class: "nav-text", "Cloud" }
                    }
                }
                button {
                    class: "add-button",
                    img { class: "plus-icon", src: "{PLUS}", alt: "New tab" }
                }
            }
        }
    }
}