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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.0/STCore.xcframework.zip",
            checksum: "9fffb5820011c5df6c4155c7b38d4b008b1999c70b9ff63208487a818cb2a227"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.0/STDatabase.xcframework.zip",
            checksum: "48fc3391bef6a8a3796c13fc33713bb0ebbc896435c6bdb9c74093b50d784dec"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.0/STNetwork.xcframework.zip",
            checksum: "fb82960de0000df2c7f5a38091e87d3e51934f396ca0e8153711c545b5d9a448"
        ),
    ]
)
