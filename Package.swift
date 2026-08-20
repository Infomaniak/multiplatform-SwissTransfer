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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.4/STCore.xcframework.zip",
            checksum: "5aa3c8189ffa53aede5782f585cf04f5a9dfcbbb1e3399b7482c7be777841315"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.4/STDatabase.xcframework.zip",
            checksum: "e0b22e2d99067f9a00124df5a37b3c208c33467740f3df1522487d7e649b77ca"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.4/STNetwork.xcframework.zip",
            checksum: "cbdaf06508a1779b3728daf6d56d5f60a5dbc515d1959dce6693e2f46de6cda6"
        ),
    ]
)
