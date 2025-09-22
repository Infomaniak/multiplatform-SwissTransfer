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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.4/STCore.xcframework.zip",
            checksum: "56f32815c75ed341c7b72d7ab058fe6cb81534b15c23eeb98779c5eab96df32a"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.4/STDatabase.xcframework.zip",
            checksum: "75fb8857c4b08920b1c7fb08d9755a42c0dc9159d9424d8a07d6ebfe9f44a1cd"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.4/STNetwork.xcframework.zip",
            checksum: "b6f0f504b14577a2aa127e9ff22ec44f0aada033e307c4fd635b38d9e2de9d66"
        ),
    ]
)
