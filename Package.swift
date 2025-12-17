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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.4.0/STCore.xcframework.zip",
            checksum: "3fa212cfb72531a30e9cfaa2c8290241e2b15e71f087a676eaa5f402ddf6f45a"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.4.0/STDatabase.xcframework.zip",
            checksum: "2fbaf4b655d69a8af5d221d68a0db444f1a43d0efdd3c57a222edd958d6c5308"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.4.0/STNetwork.xcframework.zip",
            checksum: "faf4429975db2083846d71f2dd4ed70f69c99dfd8875d7e1655386599399e08b"
        ),
    ]
)
