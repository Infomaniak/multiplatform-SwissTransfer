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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.2/STCore.xcframework.zip",
            checksum: "2de6bf69fb3bef5f391f4e74e718926faaa983908e5074b7cc0e47467204bb82"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.2/STDatabase.xcframework.zip",
            checksum: "1013d03df4388a0c2946241452870a76b519414d3ed5fe80f0848e7c08b53185"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.2/STNetwork.xcframework.zip",
            checksum: "51070baf64f562e30580e280be52ded5fc061e0777250b9404d7f3a32b102bde"
        ),
    ]
)
