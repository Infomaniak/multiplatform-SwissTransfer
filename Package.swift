// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.2/STCore.xcframework.zip",
            checksum: "147c67a81c726b666b1d264304284fa7b03ae31bc1dc6e06eaa95add7142d8d6"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.2/STDatabase.xcframework.zip",
            checksum: "e3554b00424d3bf9bf8af693756fd54f29782302f7bc79fcf280fdc4077b0ce6"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.2/STNetwork.xcframework.zip",
            checksum: "d22083a2f1d10415e0241b44da67a850996ec1cca8eaeda1e3d4ba7911bafb9f"
        ),
    ]
)
