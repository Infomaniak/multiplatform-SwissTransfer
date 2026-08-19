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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-10.1.2-dyn-202608191404-32260927930-1/STCore.xcframework.zip",
            checksum: "04d9613fcc32103f4d28ca6f2e340c02d9bbe1f9ebe42afff325bd0749815fd2"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-10.1.2-dyn-202608191404-32260927930-1/STDatabase.xcframework.zip",
            checksum: "22baa17350a2c563d6717b2121541c8ffd80eff63df20b5ccb06f2bde634285f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-10.1.2-dyn-202608191404-32260927930-1/STNetwork.xcframework.zip",
            checksum: "6da26fbf79b1586eb2713bba1e974b483aac905f06b29bc637c4bef453b32f24"
        ),
    ]
)
